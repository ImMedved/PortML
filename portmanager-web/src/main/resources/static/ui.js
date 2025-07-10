/* =================================================================== *
 *  ui.js  –  top panel, dialog boxes, CRUD logic
 * =================================================================== */

import { $, boardMl, boardManual,
        lastScenario, lastPlan, mlSchedule, manualSchedule,
        cooldown, api, setStatus, exportPNG,
        resetStateFromScenario, shipMap,
        countersLbl, statusLbl,
        setLastPlan, setMLSchedule, setManualSchedule,
        fmt, hMs
} from './core.js';

import {
    buildSchedule, renderAll,
    renderScheduleTable, renderShipTable
} from './gantt.js';

/* ------------------------------------------------------------------- *
 *  RANDOM  /  ADVANCED Generators
 * ------------------------------------------------------------------- */
$('btnRandom').onclick = async ()=>{
    if(!cooldown('rnd')) return;
    setStatus('Random …');
    try{
        const dto = await api('/random?ships=0',{method:'POST'});
        resetStateFromScenario(dto);
        renderShipTable();
        setStatus('Scenario ready');
    }catch(e){ alert(e); setStatus('Error'); }
};

/* ---------- Advanced Generator ---------- */
$('btnAdvanced').onclick = ()=> dlgGen.showModal();

$('btnGenOk').onclick = e=>{
    e.preventDefault();
    const f = new FormData(formGen);

    const gen = {
        shipCount        : +f.get('shipCount')||0,
        pilotPercent     : +f.get('pilotPercent'),
        customsPercent   : +f.get('customsPercent'),
        priorityPercent  : +f.get('priorityPercent'),
        temperaturePercent:+f.get('temperaturePercent'),
        cargoDistribution:{
            container:+f.get('cargo_container'), bulk:+f.get('cargo_bulk'),
            oil:+f.get('cargo_oil'), lng:+f.get('cargo_lng'),
            general:+f.get('cargo_general')
        },
        terminalCount:+f.get('terminalCount')||0,
        terminalCargoDistribution:{
            universal:+f.get('term_univ'), container:+f.get('term_cont'),
            bulk:+f.get('term_bulk'), oil:+f.get('term_oil'),
            lng:+f.get('term_lng')
        }
    };

    dlgGen.close();
    setStatus('Custom …');
    api('/custom',{method:'POST',body:JSON.stringify(gen)})
       .then(dto=>{ resetStateFromScenario(dto);
                    renderShipTable();
                    setStatus('Scenario ready'); })
       .catch(err=>{alert(err); setStatus('Error');});
};

/* ------------------------------------------------------------------- *
 *  PLAN (ML)   /   COPY to manual
 * ------------------------------------------------------------------- */
$('btnPlan').onclick = async ()=>{
    if(!cooldown('plan')||!lastScenario){
        alert('Generate scenario first'); return;
    }
    setStatus('Planning …');
    try{
        const plan = await api(`/plan?alg=${$('algorithm').value}`,{method:'POST'});
        setLastPlan(plan);
        setMLSchedule(buildSchedule(plan));
        setManualSchedule([]);
        renderAll();
        setStatus(`ID:${plan.scenarioId} · ${plan.algorithmUsed}`);
    }catch(e){ alert(e); setStatus('Error'); }
};

$('btnCopy').onclick = ()=>{
    if(!mlSchedule?.length){ alert('No ML plan'); return; }
    setManualSchedule(structuredClone(mlSchedule));
    renderAll();
    setStatus('ML plan copied → manual');
};

$('btnManualClear').onclick = ()=>{
    setManualSchedule([]);
    renderAll();
    setStatus('Manual cleared');
};

/* ------------------------------------------------------------------- *
 *  PNG-export
 * ------------------------------------------------------------------- */
$('btnExport').onclick       = ()=> exportPNG(boardMl,'ml_plan');
$('btnExportManual').onclick = ()=> exportPNG(boardManual,'manual_plan');

/* ------------------------------------------------------------------- *
 *  CRUD-dialogs  (Terminals / Ships / Events)
 * ------------------------------------------------------------------- */
$('btnTerminals').onclick = ()=>{ fillTerminalTable(); dlgTerm.showModal(); };
$('btnShips').onclick     = ()=>{ fillShipCrud();     dlgShips.showModal(); };
$('btnEvents').onclick    = ()=>{ fillEventCrud();    dlgEvents.showModal(); };

/* ---------- TERMINALS ------------------------------------------------ */
$('btnTermAdd').onclick = ()=>{
    const id = Date.now();
    lastScenario.terminals.push({
        id, name:'T'+id, length:250, draft:10,
        cargo:'container', fuel:'diesel',
        maxLength:250, maxDraft:10,
        allowedCargoTypes:['container'], fuelSupported:['diesel']
    });
    fillTerminalTable();
};

$('btnTermDel').onclick = ()=>{
    const sel = document.querySelector('#tblTerminals tbody tr.selected');
    if(!sel) return;
    const id = +sel.dataset.id;
    lastScenario.terminals =
        lastScenario.terminals.filter(t=>t.id!==id);
    fillTerminalTable();
};

function fillTerminalTable(){
    if(!lastScenario){ alert('Generate scenario first'); return; }
    const tb = document.querySelector('#tblTerminals tbody');    tb.innerHTML='';
    lastScenario.terminals.forEach(t=>{
        const tr=document.createElement('tr');
        tr.dataset.id=t.id;
        tr.innerHTML=`
           <td>${t.id}</td>
           <td contenteditable>${t.name}</td>
           <td contenteditable>${t.length  ?? t.maxLength  ?? ''}</td>
           <td contenteditable>${t.draft   ?? t.maxDraft   ?? ''}</td>
           <td contenteditable>${t.cargo   ?? (t.allowedCargoTypes?.join(',') || '')}</td>
           <td contenteditable>${t.fuel    ?? (t.fuelSupported?.join(',')    || '')}</td>
           <td></td>`;
        tr.onclick=()=>{ tb.querySelectorAll('tr').forEach(r=>r.classList.remove('selected'));
                          tr.classList.add('selected'); };
        tb.append(tr);
    });
}

/* ---------- SHIPS ---------------------------------------------------- */
$('btnShipAdd').onclick = ()=>{
    const row = {
        id:'V'+Date.now(), arrivalTime:new Date().toISOString(),
        length:150, draft:7,
        shipType:'container', cargoType:'container',
        fuelType:'diesel', priority:'normal',
        estDurationHours:12
    };
    lastScenario.ships.push(row);
    fillShipCrud();
};

$('btnShipDel').onclick = ()=>{
    const sel = document.querySelector('#tblShips tbody tr.selected');
    if(!sel) return;
    const id = sel.dataset.id;
    lastScenario.ships = lastScenario.ships.filter(s=>String(s.id)!==id);
    fillShipCrud();
};

function fillShipCrud(){
    if(!lastScenario){ alert('Generate scenario first'); return; }
    const tb = document.querySelector('#tblShips tbody');
    tb.innerHTML='';
    lastScenario.ships.forEach(s=>{
        const tr=document.createElement('tr');
        tr.dataset.id=s.id;
        tr.innerHTML=`
          <td>${s.id}</td>
          <td contenteditable>${fmt(s.arrivalTime)}</td>
          <td contenteditable>${s.length}</td>
          <td contenteditable>${s.draft}</td>
          <td contenteditable>${s.shipType}</td>
          <td contenteditable>${s.cargoType}</td>
          <td contenteditable>${s.fuelType}</td>
          <td contenteditable>${s.priority}</td>
          <td></td>`;
        tr.onclick=()=>{ tb.querySelectorAll('tr').forEach(r=>r.classList.remove('selected'));
                         tr.classList.add('selected'); };
        tb.append(tr);
    });
}

/* ---------- EVENTS --------------------------------------------------- */
$('btnEventAdd').onclick = ()=>{
    const now = Date.now();
    const ev = {
        eventType:'WEATHER',
        start:new Date(now).toISOString(),
        end  :new Date(now+3*hMs).toISOString(),
        terminalId:''
    };
    lastScenario.events.push(ev);
    fillEventCrud();
};

$('btnEventDel').onclick = ()=>{
    const sel = document.querySelector('#tblEvents tbody tr.selected');
    if(!sel) return;
    const idx = [...sel.parentNode.children].indexOf(sel);
    lastScenario.events.splice(idx,1);
    fillEventCrud();
};

function fillEventCrud(){
    if(!lastScenario){
        alert('Generate scenario first');
        return;
    }
    const tb = document.querySelector('#tblEvents tbody');
    tb.innerHTML='';
    lastScenario.events.forEach((e,i)=>{
        const tr=document.createElement('tr');
        tr.innerHTML=`
          <td contenteditable>${e.eventType}</td>
          <td contenteditable>${e.terminalId||''}</td>
          <td contenteditable>${fmt(e.start)}</td>
          <td contenteditable>${fmt(e.end)}</td>
          <td></td>`;
        tr.onclick=()=>{ tb.querySelectorAll('tr').forEach(r=>r.classList.remove('selected'));
                         tr.classList.add('selected'); };
        tb.append(tr);
    });
}

console.log('UI module loaded');