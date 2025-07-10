/* =================================================================== *
 *  PortManager‑Web – enhanced client logic (v2)
 *  ‣ lane‑packing (multi‑rows per terminal)
 *  ‣ virtual lanes CUSTOMS / RAID + waiting intervals
 *  ‣ ML → Manual copy, manual clear, manual PNG export
 *  ‣ status label & plan id
 *  NOTE: requires extra buttons & <span id="statusLabel"> in index.html
 * =================================================================== */

/* ---------- CONSTANTS & ELEMENTS ---------- */
const API          = '/ui';

const boardMl      = document.getElementById('boardMl');
const boardManual  = document.getElementById('boardManual');
const tblSchedule  = document.getElementById('tblSchedule').querySelector('tbody');
const countersLbl  = document.getElementById('counters');

/* dynamically added in init() if not present */
let statusLbl      = document.getElementById('statusLabel');

let lastScenario   = null;   // ConditionsDto in memory
let lastPlan       = null;   // PlanResponseDto (raw from server)
let mlSchedule     = null;   // extended schedule for ML board (with RAID etc.)
let manualSchedule = null;   // copy for manual editing

/* ---------- HELPERS ---------- */
const fmt = iso => new Date(iso).toLocaleString();

const withCooldown = (() => {
  const stamp = new Map();
  return (id, ms = 5000) => {
    const now = Date.now();
    if (now - (stamp.get(id) || 0) < ms) return false;
    stamp.set(id, now);
    return true;
  };
})();

async function api(path, opt = {}) {
  const res = await fetch(API + path, {headers: {'Content-Type': 'application/json'}, ...opt});
  if (!res.ok) throw new Error(`${res.status} ${res.statusText}`);
  return res.status === 204 ? null : res.json();
}

function updateCounters(scn) {
  countersLbl.textContent = `T:${scn.terminals.length} V:${scn.ships.length} E:${scn.events.length}`;
}

function setStatus(msg) {
  if (!statusLbl) return;
  statusLbl.textContent = msg;
}

/* ---------- TOOLBAR AUGMENTATION ---------- */
(function augmentToolbar(){
  // status label
  if (!statusLbl){
    statusLbl = document.createElement('span');
    statusLbl.id = 'statusLabel';
    statusLbl.style.marginLeft = 'auto';
    document.querySelector('.toolbar').append(statusLbl);
  }

  // buttons only if they don't exist yet
  const toolbar = document.querySelector('.toolbar');
  if (!document.getElementById('btnCopy')){
    const btn = document.createElement('button');
    btn.id = 'btnCopy'; btn.textContent = 'Fix ML→Manual';
    btn.onclick = copyMlToManual;
    toolbar.insertBefore(btn, document.getElementById('btnExport'));
  }
  if (!document.getElementById('btnManualClear')){
    const btn = document.createElement('button');
    btn.id = 'btnManualClear'; btn.textContent = 'Clear manual';
    btn.onclick = ()=>{ manualSchedule=[]; renderAll(); setStatus('Manual plan cleared'); };
    toolbar.insertBefore(btn, document.getElementById('btnExport'));
  }
  if (!document.getElementById('btnExportManual')){
    const btn = document.createElement('button');
    btn.id='btnExportManual'; btn.textContent='Export Manual PNG';
    btn.onclick = ()=> exportBoardPNG(boardManual,'manual_plan');
    toolbar.insertBefore(btn, document.getElementById('btnExport'));
  }
})();

/* ---------- RANDOM DATA ---------- */
document.getElementById('btnRandom').onclick = async () => {
  if (!withCooldown('random')) return;
  const n = prompt('Ships count?', 10);
  if (!n) return;
  try {
    setStatus('Loading random scenario…');
    lastScenario = await api(`/random?ships=${n}`, {method: 'POST'});
    lastPlan = manualSchedule = mlSchedule = null;
    updateCounters(lastScenario);
    clearBoards();
    setStatus('Scenario ready');
  } catch (e) { alert(e); setStatus('Error'); }
};

/* ---------- ADVANCED GENERATION ---------- */
const formGen = document.getElementById('formGen');
document.getElementById('btnGenOk').onclick = async e => {
  e.preventDefault();
  const cfg = Object.fromEntries(new FormData(formGen).entries());
  dlgGen.close();
  try {
    setStatus('Loading custom scenario…');
    lastScenario = await api('/custom', {method:'POST', body: JSON.stringify(cfg)});
    lastPlan = manualSchedule = mlSchedule = null;
    updateCounters(lastScenario);
    clearBoards();
    setStatus('Scenario ready');
  } catch (err) { alert(err); setStatus('Error'); }
};

document.getElementById('btnAdvanced').onclick = ()=> dlgGen.showModal();

/* ---------- PLAN ---------- */
document.getElementById('btnPlan').onclick = async () => {
  if (!withCooldown('plan')) return;
  if (!lastScenario) { alert('Generate scenario first'); return; }
  const alg = document.getElementById('algorithm').value;
  try {
    setStatus('Requesting plan…');
    lastPlan = await api(`/plan?alg=${alg}`, {method:'POST'});
    // extend schedule with customs/raid & lanes
    mlSchedule = prepareSchedule(lastPlan, lastScenario);
    manualSchedule = structuredClone(mlSchedule);
    renderAll();
    setStatus(`Plan ${lastPlan.scenarioId} · ${lastPlan.algorithmUsed}`);
  } catch (e) { alert(e); setStatus('Error'); }
};

/* ---------- PNG export ---------- */
async function exportBoardPNG(el, baseName){
  const {toBlob} = await import('https://cdn.jsdelivr.net/npm/html-to-image/+esm');
  const png = await toBlob(el, {pixelRatio: 2});
  const a = Object.assign(document.createElement('a'), {
    href: URL.createObjectURL(png),
    download: baseName + '.png'
  });
  a.click();
}

document.getElementById('btnExport').onclick = ()=> exportBoardPNG(boardMl,'ml_plan');

/* ---------- ML → Manual ---------- */
function copyMlToManual(){
  if (!mlSchedule){ alert('No ML plan yet'); return; }
  manualSchedule = structuredClone(mlSchedule);
  renderAll();
  setStatus('ML plan copied → manual');
}

/* =================================================================== *
 *  SCHEDULE PREPARATION (CUSTOMS / RAID + lane packing helper data)
 * =================================================================== */
function prepareSchedule(plan, scenario){
  const shipsById = Object.fromEntries(scenario.ships.map(s => [s.id, s]));

  const items = []; // result schedule

  // 1) map terminal 0 → CUSTOMS and push originals
  plan.schedule.forEach(s => {
    const mapped = {...s};
    if (mapped.terminalId === '0' || mapped.terminalId === 0) mapped.terminalId = 'CUSTOMS';
    items.push(mapped);
  });

  // 2) add RAID waiting intervals when ETA < service start
  items.slice().forEach(si => {
    const ship = shipsById[si.vesselId];
    if (!ship) return;
    const eta = new Date(ship.arrivalTime).getTime() + (ship.expectedDelayHours||0)*3600e3;
    const start = Date.parse(si.startTime);
    if (start - eta > 1){
      items.push({
        terminalId: 'RAID',
        vesselId:   si.vesselId,
        shipName:   si.shipName || si.vesselId,
        start:      new Date(eta).toISOString(),
        end:        si.startTime,
      });
    }
  });

  // 3) normalise fields start/end (camelCase) for rendering helpers
  return items.map(it => ({
    ...it,
    start: it.start || it.startTime,
    end:   it.end   || it.endTime,
    shipId: it.vesselId,
    shipName: it.shipName || it.vesselId,
  }));
}

/* =================================================================== *
 *  RENDERING
 * =================================================================== */
function clearBoards(){
  boardMl.innerHTML = boardManual.innerHTML = tblSchedule.innerHTML = '';
}

function renderAll(){
  renderBoard(boardMl,     mlSchedule,     false);
  renderBoard(boardManual, manualSchedule, true );
  renderSchedule(manualSchedule);
}

/**
 * Grid‑based Gantt with lane‑packing per terminal.
 */
function renderBoard(container, items, draggable){
  container.innerHTML='';
  if (!items?.length) return;

  /* ---------- time horizon ---------- */
  const hMs = 3600e3;
  const minStart = Math.min(...items.map(i=>Date.parse(i.start)));
  const maxEnd   = Math.max(...items.map(i=>Date.parse(i.end)));
  const totalHours = Math.ceil((maxEnd - minStart)/hMs) + 24; // headroom 1d

  /* ---------- lanes calculation ---------- */
  const terms = [...new Set(items.map(i=>i.terminalId))];
  const lanesByTerm = {};
  items.forEach(it=>{
    const term = it.terminalId;
    lanesByTerm[term] ||= [];
    let laneIdx = lanesByTerm[term].findIndex(lane=> lane.every(x=> !(Date.parse(it.start)<Date.parse(x.end) && Date.parse(x.start)<Date.parse(it.end)) ));
    if (laneIdx === -1){ laneIdx = lanesByTerm[term].length; lanesByTerm[term].push([]); }
    it.__lane = laneIdx;
    lanesByTerm[term][laneIdx].push(it);
  });

  /* ---------- grid template ---------- */
  const rowOffset={}; let offset=0;
  terms.forEach(t=>{ rowOffset[t]=offset; offset+=lanesByTerm[t].length; });

  container.style.gridTemplateColumns = `repeat(${totalHours},40px)`;
  container.style.gridTemplateRows    = `repeat(${offset},40px)`;

  /* ---------- draw blocks ---------- */
  items.forEach(it=>{
    const col  = Math.floor((Date.parse(it.start)-minStart)/hMs)+1;
    const span = Math.max(1, Math.round((Date.parse(it.end)-Date.parse(it.start))/hMs));
    const row  = rowOffset[it.terminalId] + it.__lane + 1;

    const div = document.createElement('div');
    div.className = 'block' + (draggable?' manual':'');
    div.textContent = it.shipName;
    div.style.gridColumn = `${col} / span ${span}`;
    div.style.gridRow    = row;
    div.title = `${it.shipName}\n${fmt(it.start)} – ${fmt(it.end)}\nT: ${it.terminalId}`;
    div.onclick = () => openShip(it.shipId);
    container.append(div);

    if (draggable) enableDnD(div,it,minStart,hMs,row);
  });
}

/* ---------- DRAG‑N‑DROP for manual board ---------- */
function enableDnD(el, item, origin, hMs, fixedRow){
  interact(el).draggable({
    listeners:{
      start(){ el.dataset.col = parseInt(el.style.gridColumn.split('/')[0]); },
      move(e){
        const dx = Math.round(e.dx/40);
        const cur = parseInt(el.style.gridColumnStart || el.style.gridColumn.split('/')[0]);
        el.style.gridColumnStart = cur + dx;
      },
      end(){
        // recompute start/end
        const newCol = parseInt(el.style.gridColumn.split('/')[0]);
        const newStart = new Date(origin + (newCol-1)*hMs);
        const durMs = Date.parse(item.end) - Date.parse(item.start);
        item.start = newStart.toISOString();
        item.end   = new Date(newStart.getTime()+durMs).toISOString();
        renderSchedule(manualSchedule);
        saveManualDebounced();
      }
    },
    modifiers:[ interact.modifiers.snap({targets:[{x:0,y:0,range:20}]}) ]
  });
}

/* ---------- SCHEDULE TABLE ---------- */
function renderSchedule(items){
  tblSchedule.innerHTML='';
  if (!items?.length) return;
  items.sort((a,b)=> Date.parse(a.start)-Date.parse(b.start));
  items.forEach(it=>{
    const tr = document.createElement('tr');
    tr.innerHTML = `<td><a href="#" data-id="${it.shipId}">${it.shipName}</a></td>
                    <td>${it.terminalId}</td>
                    <td>${fmt(it.start)}</td>
                    <td>${fmt(it.end)}</td>`;
    tr.querySelector('a').onclick=e=>{e.preventDefault();openShip(it.shipId);} ;
    tblSchedule.append(tr);
  });
}

/* ---------- SHIP INFO ---------- */
async function openShip(id){
  if (!id) return;
  try {
    const ship = await api(`/ship/${id}`);
    document.getElementById('shipContent').textContent = `\nArrival: ${ship.arrivalTime}\nLen/Draft: ${ship.length}/${ship.draft}\nCargo/Type: ${ship.cargoType}/${ship.shipType}\nDuration h: ${ship.estDurationHours}\nDeadweight: ${ship.deadweight}`;
    dlgShip.showModal();
  }catch(e){ alert(e); }
}

/* ---------- SAVE manual plan (debounced) ---------- */
const saveManualDebounced = (()=>{ let t; return ()=>{ clearTimeout(t); t=setTimeout(saveManualPlan,1000);} })();
async function saveManualPlan(){
  try{
    await api('/plan/manual',{method:'PUT',body:JSON.stringify(manualSchedule)});
    setStatus('Manual plan saved');
  }catch(e){ console.error(e); setStatus('Save failed'); }
}

/* ---------- INIT LOG ---------- */
console.log('PortManager‑Web enhanced version loaded');
