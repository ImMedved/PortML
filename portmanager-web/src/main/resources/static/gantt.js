/* =================================================================== *
 *  gantt.js  –  diagrams, DnD, tables
 * =================================================================== */

import {
    boardMl, boardManual, tblScheduleBody, tblShipsBody,
    lastScenario, lastPlan,
    mlSchedule, manualSchedule,
    offsetManual, laneCount,
    map, hMs, fmt, saveManualDebounced, exportPNG, shipMap,
    setStatus, resetStateFromScenario
} from './core.js';

/* ------------------------------------------------------------------- *
 *  Building a unified schedule (CUSTOMS, RAID, standby)
 * ------------------------------------------------------------------- */
export function buildSchedule(plan){
    const ships = shipMap();
    const out   = [];

    /* 1. original intervals, terminalId "0" → CUSTOMS */
    plan.schedule.forEach(s=>{
        const term = s.terminalId==='0' ? 'CUSTOMS' : s.terminalId;
        out.push({...s,
                  terminalId:term,
                  start     :s.startTime,
                  end       :s.endTime,
                  shipId    :s.vesselId,
                  shipName  :s.vesselId});
    });

    /* 2. RAID – waiting until service starts */
    out.slice().forEach(si=>{
        const eta = new Date(ships[si.shipId].arrivalTime).getTime();
        const st  = Date.parse(si.start);
        if(st > eta){
            out.push({
                terminalId:'RAID',
                shipId    :si.shipId,
                shipName  :si.shipName,
                start     :new Date(eta).toISOString(),
                end       :si.start
            });
        }
    });

    return out;
}

/* ------------------------------------------------------------------- *
 *  Rerender
 * ------------------------------------------------------------------- */
export function renderAll(){
    const ev = lastScenario?.events || [];
    renderBoard(boardMl,     mlSchedule,     false, ev);
    renderBoard(boardManual, manualSchedule, true , ev);
    renderScheduleTable();
    renderShipTable();
}

/* ------------------------------------------------------------------- *
 *  Gantt-board (CSS Grid + strip compaction)
 * ------------------------------------------------------------------- */
export function renderBoard(el, items, draggable, events){
    el.innerHTML='';
    if(!items?.length) return;

    /* time horizon */
    const min   = Math.min(...items.map(i=>Date.parse(i.start)));
    const max   = Math.max(...items.map(i=>Date.parse(i.end)));
    const hours = Math.ceil((max-min)/hMs)+24;  // +1 days

    /* terminal sealing */
    const terms  = [...new Set(items.map(i=>i.terminalId))];
    const lanes  = {};
    const offset = {};
    let rows = 0;

    items.forEach(it=>{
        const t = it.terminalId;
        lanes[t] ??= [];

        let ln = lanes[t].findIndex(l=>
            l.every(x=>!(Date.parse(it.start)<Date.parse(x.end) &&
                         Date.parse(x.start)<Date.parse(it.end))));
        if(ln<0){ ln = lanes[t].length; lanes[t].push([]); }
        it.__lane = ln; lanes[t][ln].push(it);
    });

    terms.forEach(t=>{ offset[t]=rows; rows+=lanes[t].length; });
    el.style.gridTemplateColumns = `repeat(${hours},28px)`;
    el.style.gridTemplateRows    = `repeat(${rows},32px)`;

    /* --- save maps for DnD validation (manual only) ---------- */
    if(draggable){
        Object.assign(offsetManual, offset);
        Object.assign(laneCount,
            Object.fromEntries(Object.entries(lanes).map(([k,v])=>[k,v.length])));
    }

    /* --- EVENTS ------------------------------------------------------ */
    events?.forEach(ev=>{
        if(!ev.start||!ev.end) return;
        const col  = Math.floor((Date.parse(ev.start)-min)/hMs)+1;
        const span = Math.max(1,Math.round((Date.parse(ev.end)-Date.parse(ev.start))/hMs));

        let row, rowSpan;
        if(ev.eventType==='WEATHER' || !ev.terminalId){
            row=1; rowSpan=rows;
        }else{
            const off = offset[ev.terminalId];
            if(off===undefined) return;
            row = off+1; rowSpan=lanes[ev.terminalId].length;
        }

        const div = document.createElement('div');
        div.className = ev.eventType==='WEATHER' ? 'event-weather':'event-closure';
        div.style.gridColumn=`${col}/span ${span}`;
        div.style.gridRow   =`${row}/span ${rowSpan}`;
        el.append(div);
    });

    /* --- BLOCKS (vessels) ---------------------------------------------- */
    items.forEach(it=>{
        const col  = Math.floor((Date.parse(it.start)-min)/hMs)+1;
        const span = Math.max(1,Math.round((Date.parse(it.end)-Date.parse(it.start))/hMs));
        const row  = offset[it.terminalId] + it.__lane + 1;

        const box = document.createElement('div');
        box.className = 'block' + (draggable?' manual':'');
        box.textContent = it.shipName;
        box.style.gridColumn=`${col}/span ${span}`;
        box.style.gridRow   = row;
        box.title = `${it.shipName}\n${fmt(it.start)} – ${fmt(it.end)}\nT: ${it.terminalId}`;
        box.onclick = ()=> openShip(it.shipId);
        el.append(box);

        if(draggable) enableDrag(box,it,min);
    });
}

/* ------------------------------------------------------------------- *
 *  Drag-and-drop on manual-board
 * ------------------------------------------------------------------- */
const interact = window.interact;

function enableDrag(box,item,minEpoch){
    interact(box).draggable({
        listeners:{
            move(e){
                const dx = Math.round(e.dx/28);
                const cur = parseInt(box.style.gridColumnStart ||
                                     box.style.gridColumn.split('/')[0]);
                box.style.gridColumnStart = cur + dx;
            },
            end(){
                const col     = parseInt(box.style.gridColumn.split('/')[0]);
                const newStart= new Date(minEpoch + (col-1)*hMs);
                const dur     = Date.parse(item.end)-Date.parse(item.start);
                const newEnd  = new Date(newStart.getTime()+dur);

                const rowIdx  = parseInt(box.style.gridRowStart ||
                                          box.style.gridRow.split('/')[0]) - 1;
                const termId  = manualRowToTerminal(rowIdx);

                const reason = validateMove(item.shipId,termId,newStart,newEnd);
                if(reason){
                    alert('✗ '+reason);
                    box.style.gridColumnStart = col;
                }else{
                    item.terminalId = termId;
                    item.start = newStart.toISOString();
                    item.end   = newEnd  .toISOString();
                    renderScheduleTable();
                    saveManualDebounced();
                }
            }
        }
    });
}

/* row-index → terminalId (on saved offset/laneCount) */
function manualRowToTerminal(row){
    for(const [tid,off] of Object.entries(offsetManual)){
        if(row>=off && row<off+laneCount[tid]) return tid;
    }
    return null;
}

/* Validation of movement */
function validateMove(shipId,termId,start,end){
    const s  = map.ships[shipId];
    const tr = map.terms[termId];
    if(!s||!tr) return 'unknown data';

    if(start < new Date(s.arrivalTime))                   return 'vessel not arrived';
    if(s.length  > tr.maxLength)                          return 'length > berth';
    if(s.draft   > tr.maxDraft)                           return 'draft > berth';
    if(!tr.allowedCargoTypes.includes(s.cargoType))       return 'cargo mismatch';
    if(tr.fuelSupported && !tr.fuelSupported.includes(s.fuelType)) return 'fuel mismatch';

    /* events */
    for(const ev of map.events){
        if(ev.terminalId && ev.terminalId!==termId) continue;
        const evS=new Date(ev.start), evE=new Date(ev.end);
        if(start<evE && evS<end){
            return ev.eventType==='WEATHER' ? 'weather closed' : 'terminal closed';
        }
    }
    return null;
}

/* ------------------------------------------------------------------- *
 *  Schedule table
 * ------------------------------------------------------------------- */
export function renderScheduleTable(){
    const src = (manualSchedule&&manualSchedule.length)?manualSchedule:mlSchedule;
    tblScheduleBody.innerHTML='';
    if(!src?.length) return;

    src.slice().sort((a,b)=>Date.parse(a.start)-Date.parse(b.start))
       .forEach(it=>{
           const tr = document.createElement('tr');
           tr.innerHTML=
               `<td><a href="#">${it.shipName}</a></td>
                <td>${it.terminalId}</td>
                <td>${fmt(it.start)}</td>
                <td>${fmt(it.end)}</td>`;
           tr.querySelector('a').onclick=e=>{
               e.preventDefault(); openShip(it.shipId);
           };
           tblScheduleBody.append(tr);
       });
}

/* ------------------------------------------------------------------- *
 *  Complete table of courts
 * ------------------------------------------------------------------- */
export function renderShipTable(){
    tblShipsBody.innerHTML='';
    if(!lastScenario) return;

    lastScenario.ships.forEach(s=>{
        const row = [
            fmt(s.arrivalTime), s.id, s.cargoType, s.shipType, s.priority,
            s.length, s.draft, s.deadweight, s.estDurationHours,
            s.flagCountry, s.imoNumber, s.fuelType, s.emissionRating,
            s.arrivalPort, s.nextPort,
            s.requiresCustomsClearance?'✓':'',
            s.requiresPilot?'✓':'',
            s.hazardClass,
            s.temperatureControlled?'✓':'',
            s.arrivalWindowStart||'', s.arrivalWindowEnd||'',
            s.expectedDelayHours||0
        ];
        const tr=document.createElement('tr');
        tr.innerHTML = row.map(x=>`<td>${x??''}</td>`).join('');
        tblShipsBody.append(tr);
    });
}

/* ------------------------------------------------------------------- *
 *  Vessel information window
 * ------------------------------------------------------------------- */
export async function openShip(id){
    if(!id) return;
    const dto = await api(`/ship/${id}`);
    $('shipContent').textContent = JSON.stringify(dto,null,2);
    dlgShip.showModal();
}

/* --- debounced manual plan save -------------------------------------- */
function saveManualDeb() {
    clearTimeout(saveManualDeb._t);
    saveManualDeb._t = setTimeout(() =>
        api('/plan/manual', { method: 'PUT', body: JSON.stringify(manualSchedule) }),
        800
    );
}