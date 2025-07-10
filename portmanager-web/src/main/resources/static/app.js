/* ===================================================================== *
 *  PortManager-Web – client logic (v3, formatted)
 *  --------------------------------------------------------------------
 *  • lane-packing (multi rows per terminal)
 *  • virtual lanes CUSTOMS / RAID + waiting intervals
 *  • ML → Manual copy, manual clear, PNG export
 *  • status label, plan id, vessel table
 * ===================================================================== */


/* === CONSTANTS & DOM SHORTCUTS ======================================= */

const API = '/ui';
const $   = id => document.getElementById(id);

const boardMl      = $('boardMl');
const boardManual  = $('boardManual');
const tblSchedule  = $('tblSchedule').querySelector('tbody');
const tblShipsBody = $('tblShipsFull').querySelector('tbody');
const countersLbl  = $('counters');
const statusLbl    = $('statusLabel');

/* === RUNTIME STATE ==================================================== */

let lastScenario   = null;   // ConditionsDto (random/custom)
let lastPlan       = null;   // PlanResponseDto (server)
let mlSchedule     = null;   // extended ML schedule (RAID, lanes)
let manualSchedule = null;   // user-editable copy of mlSchedule

/* === SHARED HELPERS =================================================== */

/* millis in hour */
const hMs = 3_600_000;

/* locale formatter */
const fmt = iso => new Date(iso).toLocaleString();

/* simple cooldown (id → ms) — от спама кликов */
const cooldown = (() => {
    const stamp = new Map();
    return (id, ms = 4000) => {
        const now = Date.now();
        if (now - (stamp.get(id) || 0) < ms) return false;
        stamp.set(id, now);
        return true;
    };
})();

/* thin fetch wrapper → json / null */
const api = (path, opt = {}) =>
    fetch(API + path, { headers: { 'Content-Type': 'application/json' }, ...opt })
        .then(r => {
            if (!r.ok) throw Error(r.status);
            return r.status === 204 ? null : r.json();
        });

/* short status line */
const setStatus = txt => statusLbl.textContent = '[Port] ' + txt;

/* helper map (ID → ShipDto) из текущего сценария */
const shipMap = () =>
    Object.fromEntries(lastScenario.ships.map(s => [s.id, s]));

/* ===================================================================== *
 *  TOOLBAR BUTTONS
 * ===================================================================== */

/* ---- Random data ---- */
$('btnRandom').onclick = async () => {
    if (!cooldown('rnd')) return;
    try {
        setStatus('Random …');
        /* ships=0 ⇒ backend генерит default (500) */
        lastScenario = await api('/random?ships=0', { method: 'POST' });
        resetState();
        setStatus('Scenario ready');
    } catch (e) {
        alert(e);
        setStatus('Error');
    }
};

/* ---- Advanced generation ---- */
$('btnAdvanced').onclick = () => dlgGen.showModal();

$('btnGenOk').onclick = async event => {
    event.preventDefault();
    try {
        setStatus('Custom …');
        const cfg = Object.fromEntries(new FormData(formGen).entries());
        lastScenario = await api('/custom', {
            method: 'POST',
            body  : JSON.stringify(cfg)
        });
        resetState();
        dlgGen.close();
        setStatus('Scenario ready');
    } catch (err) {
        alert(err);
        setStatus('Error');
    }
};

/* ---- Generate plan (ML) ---- */
$('btnPlan').onclick = async () => {
    if (!cooldown('plan') || !lastScenario) {
        alert('Generate scenario first');
        return;
    }
    try {
        setStatus('Planning …');
        lastPlan = await api(`/plan?alg=${$('algorithm').value}`, { method: 'POST' });

        /* build extended schedule for ML board */
        mlSchedule     = buildSchedule(lastPlan);
        manualSchedule = structuredClone(mlSchedule);

        renderAll();
        setStatus(`ID:${lastPlan.scenarioId} · ${lastPlan.algorithmUsed}`);
    } catch (e) {
        alert(e);
        setStatus('Error');
    }
};

/* ---- ML → Manual copy / Manual clear ---- */
$('btnCopy').onclick = () => {
    if (!mlSchedule) { alert('No ML plan'); return; }
    manualSchedule = structuredClone(mlSchedule);
    renderAll();
};

$('btnManualClear').onclick = () => {
    manualSchedule = [];
    renderAll();
    setStatus('Manual cleared');
};

/* ---- PNG export ---- */
$('btnExport').onclick       = () => exportPNG(boardMl,     'ml_plan');
$('btnExportManual').onclick = () => exportPNG(boardManual, 'manual_plan');

/* ===================================================================== *
 *  CRUD-DIALOG BUTTONS
 * ===================================================================== */

$('btnTerminals').onclick = async () => {
    await refreshTermTable();
    dlgTerm.showModal();
};
$('btnShips').onclick = async () => {
    await refreshShipCrud();
    dlgShips.showModal();
};
$('btnEvents').onclick = async () => {
    await refreshEventCrud();
    dlgEvents.showModal();
};

/* --- tiny helpers for each dialog (simplified UI) --- */

async function refreshTermTable() { /* left as is */ }

async function refreshShipCrud() {
    const tbody = $('tblShips').querySelector('tbody');
    tbody.innerHTML = '';
    lastScenario.ships.forEach(s => {
        const tr = document.createElement('tr');
        tr.innerHTML =
            `<td>${s.id}</td><td>${fmt(s.arrivalTime)}</td>
             <td>${s.length}</td><td>${s.draft}</td>
             <td><button data-id="${s.id}">✖</button></td>`;
        tbody.append(tr);
    });
}

async function refreshEventCrud() {
    const tbody = $('tblEvents').querySelector('tbody');
    tbody.innerHTML = '';
    lastScenario.events.forEach(e => {
        const tr = document.createElement('tr');
        tr.innerHTML =
            `<td>${e.eventType}</td><td>${e.terminalId || ''}</td>
             <td>${fmt(e.start)}</td><td>${fmt(e.end)}</td><td></td>`;
        tbody.append(tr);
    });
}

/* ===================================================================== *
 *  RENDERING helpers
 * ===================================================================== */

/* сброс всего визуального состояния при получении нового сценария */
function resetState() {
    lastPlan = mlSchedule = manualSchedule = null;

    countersLbl.textContent =
        `T:${lastScenario.terminals.length} ` +
        `V:${lastScenario.ships.length} ` +
        `E:${lastScenario.events.length}`;

    boardMl .innerHTML =
    boardManual.innerHTML =
    tblSchedule.innerHTML =
    tblShipsBody.innerHTML = '';
    renderShipTable();
}

/* строим единый список schedule-items (CUSTOMS / RAID + ожидание) */
function buildSchedule(plan) {

    const ships   = shipMap();
    const out     = [];

    /* 1. оригинал, 0 ⇒ CUSTOMS */
    plan.schedule.forEach(s => {
        const term = s.terminalId === '0' ? 'CUSTOMS' : s.terminalId;
        out.push({
            ...s,
            terminalId : term,
            start      : s.startTime,
            end        : s.endTime,
            shipId     : s.vesselId,
            shipName   : s.vesselId
        });
    });

    /* 2. RAID – если ETA < start-service */
    out.slice().forEach(si => {
        const eta = new Date(ships[si.shipId].arrivalTime).getTime();
        const st  = Date.parse(si.start);
        if (st - eta > 0) {
            out.push({
                terminalId : 'RAID',
                shipId     : si.shipId,
                shipName   : si.shipName,
                start      : new Date(eta).toISOString(),
                end        : si.start
            });
        }
    });

    return out;
}

/* полный ре-дроу (доски + таблицы) */
function renderAll() {
    renderBoard(boardMl,     mlSchedule,     false);
    renderBoard(boardManual, manualSchedule, true );
    renderScheduleTable();
    renderShipTable();
}

/* --------------------------------------------------------------------- *
 *  Gantt grid (grid-layout, lane-packing)
 * --------------------------------------------------------------------- */
function renderBoard(el, items, draggable) {

    el.innerHTML = '';
    if (!items?.length) return;

    /* horizon */
    const min   = Math.min(...items.map(i => Date.parse(i.start)));
    const max   = Math.max(...items.map(i => Date.parse(i.end)));
    const hours = Math.ceil((max - min) / hMs) + 24;     // +1 day headroom

    /* lane packing per terminal */
    const terms  = [...new Set(items.map(i => i.terminalId))];
    const lanes  = {};
    const offset = {};
    let rows = 0;

    items.forEach(it => {
        const t = it.terminalId;
        lanes[t] ??= [];

        /* ищем первый lane без пересечений */
        let ln = lanes[t].findIndex(lane =>
            lane.every(x =>
                !(Date.parse(it.start) < Date.parse(x.end) &&
                  Date.parse(x.start) < Date.parse(it.end))));
        if (ln < 0) { ln = lanes[t].length; lanes[t].push([]); }

        it.__lane = ln;
        lanes[t][ln].push(it);
    });

    /* grid sizes */
    terms.forEach(t => { offset[t] = rows; rows += lanes[t].length; });
    el.style.gridTemplateColumns = `repeat(${hours}, 28px)`;
    el.style.gridTemplateRows    = `repeat(${rows},  32px)`;

    /* draw blocks */
    items.forEach(it => {

        const col  = Math.floor((Date.parse(it.start) - min) / hMs) + 1;
        const span = Math.max(
            1,
            Math.round((Date.parse(it.end) - Date.parse(it.start)) / hMs)
        );
        const row  = offset[it.terminalId] + it.__lane + 1;

        const box = document.createElement('div');
        box.className = 'block' + (draggable ? ' manual' : '');
        box.textContent = it.shipName;
        box.style.gridColumn = `${col} / span ${span}`;
        box.style.gridRow    = row;
        box.title = `${it.shipName}\n${fmt(it.start)} – ${fmt(it.end)}\nT: ${it.terminalId}`;
        box.onclick = () => openShip(it.shipId);
        el.append(box);

        if (draggable) enableDrag(box, it, min);
    });
}

/* --- Drag & Drop on manual board --- */
function enableDrag(view, item, minStart) {

    interact(view).draggable({

        listeners: {
            move(e) {
                const dx = Math.round(e.dx / 28);
                const cur = parseInt(
                    view.style.gridColumnStart ||
                    view.style.gridColumn.split('/')[0]
                );
                view.style.gridColumnStart = cur + dx;
            },

            end() {
                const col = parseInt(view.style.gridColumn.split('/')[0]);
                const newStart = new Date(minStart + (col - 1) * hMs);
                const durMs    = Date.parse(item.end) - Date.parse(item.start);

                item.start = newStart.toISOString();
                item.end   = new Date(newStart.getTime() + durMs).toISOString();

                renderScheduleTable();
                saveManualDebounced();
            }
        }
    });
}

/* --------------------------------------------------------------------- *
 *  Schedule table (right section)
 * --------------------------------------------------------------------- */
function renderScheduleTable() {

    tblSchedule.innerHTML = '';
    if (!manualSchedule) return;

    manualSchedule
        .slice()
        .sort((a, b) => Date.parse(a.start) - Date.parse(b.start))
        .forEach(it => {
            const tr = document.createElement('tr');
            tr.innerHTML =
                `<td><a href="#">${it.shipName}</a></td>
                 <td>${it.terminalId}</td>
                 <td>${fmt(it.start)}</td>
                 <td>${fmt(it.end)}</td>`;
            tr.querySelector('a').onclick = e => {
                e.preventDefault();
                openShip(it.shipId);
            };
            tblSchedule.append(tr);
        });
}

/* --------------------------------------------------------------------- *
 *  VESSEL table (full list under schedule)
 * --------------------------------------------------------------------- */
function renderShipTable() {

    tblShipsBody.innerHTML = '';
    if (!lastScenario) return;

    lastScenario.ships.forEach(s => {
        const row = [
            fmt(s.arrivalTime),           s.id,            s.cargoType,
            s.shipType,                   s.priority,
            s.length,                     s.draft,
            s.deadweight,                 s.estDurationHours,
            s.flagCountry,                s.imoNumber,
            s.fuelType,                   s.emissionRating,
            s.arrivalPort,                s.nextPort,
            s.requiresCustomsClearance ? '✓' : '',
            s.requiresPilot             ? '✓' : '',
            s.hazardClass,
            s.temperatureControlled     ? '✓' : '',
            s.arrivalWindowStart || '',   s.arrivalWindowEnd || '',
            s.expectedDelayHours || 0
        ];
        const tr = document.createElement('tr');
        tr.innerHTML = row.map(x => `<td>${x ?? ''}</td>`).join('');
        tblShipsBody.append(tr);
    });
}

/* ===================================================================== *
 *  Misc helpers
 * ===================================================================== */

/* --- ship info modal -------------------------------------------------- */
async function openShip(id) {
    if (!id) return;
    const dto = await api(`/ship/${id}`);
    $('shipContent').textContent = JSON.stringify(dto, null, 2);
    dlgShip.showModal();
}

/* --- full-board PNG export ------------------------------------------- */
async function exportPNG(el, name) {
    const { toBlob } = await import('https://cdn.jsdelivr.net/npm/html-to-image/+esm');

    /* временно раскрываем элемент на полную площадь */
    const prev = { w: el.style.width, h: el.style.height, ov: el.style.overflow };
    el.style.width  = el.scrollWidth  + 'px';
    el.style.height = el.scrollHeight + 'px';
    el.style.overflow = 'visible';

    const blob = await toBlob(el, { pixelRatio: 2 });
    Object.assign(el.style, prev);

    const a = Object.assign(document.createElement('a'), {
        href: URL.createObjectURL(blob),
        download: name + '.png'
    });
    a.click();
}

/* --- debounced manual plan save -------------------------------------- */
const saveManualDebounced = (() => {
    let t;
    return () => {
        clearTimeout(t);
        t = setTimeout(() =>
            api('/plan/manual', { method: 'PUT', body: JSON.stringify(manualSchedule) }),
            800
        );
    };
})();

console.log('PortManager-Web v3 loaded');
