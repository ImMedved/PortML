/* =================================================================== *
 *  core.js  –  general data and helper functions
 * =================================================================== */
export const API = '/ui';
export const $   = id => document.getElementById(id);

/* ----- DOM links (created after index.html is loaded) ------------- */
export const boardMl         = $('boardMl');
export const boardManual     = $('boardManual');
export const tblScheduleBody = document.querySelector('#tblSchedule tbody');
export const tblShipsBody    = document.querySelector('#tblShipsFull tbody');
export const countersLbl     = $('counters');
export const statusLbl       = $('statusLabel');

/* ----- Global state ---------------------------------------- */
export let lastScenario   = null;   // ConditionsDto
export let lastPlan       = null;   // PlanResponseDto
export let mlSchedule     = null;   // ML-graph
export let manualSchedule = [];     // user edit

export let offsetManual = {};       // helpers for DnD
export let laneCount    = {};

export const map = { ships:{}, terms:{}, events:[] };

/* ----- Various useful things ------------------------------------------- */
export const hMs = 3_600_000;
export const fmt = iso => new Date(iso).toLocaleString();

export const cooldown = (()=>{
    const stamp = new Map();
    return (id, ms = 4000)=>{
        const now = Date.now();
        if(now - (stamp.get(id) || 0) < ms) return false;
        stamp.set(id, now); return true;
    };
})();

export const api = (path, opt={})=>
    fetch(API+path,{headers:{'Content-Type':'application/json'},...opt})
        .then(r=>{ if(!r.ok) throw Error(r.status);
                   return r.status===204?null:r.json(); });

export const setStatus = txt=>{
    if(statusLbl) statusLbl.textContent = '[Port] '+txt;
};

export const shipMap = ()=>                            // {id -> Ship}
    Object.fromEntries(lastScenario.ships.map(s=>[s.id,s]));

/* ----- save manual plan with delay ------------------------- */
export const saveManualDebounced = (()=>{ let t;
    return ()=>{ clearTimeout(t);
        t = setTimeout(()=> api('/plan/manual',
            {method:'PUT',body:JSON.stringify(manualSchedule)}),
            800);
    };
})();

/* ----- PNG export of the whole board -------------------------------------- */
export async function exportPNG(el,name){
    const {toBlob} = await import('https://cdn.jsdelivr.net/npm/html-to-image/+esm');
    const prev = {w:el.style.width,h:el.style.height,ov:el.style.overflow};
    el.style.width = el.scrollWidth+'px';
    el.style.height= el.scrollHeight+'px';
    el.style.overflow='visible';

    const blob = await toBlob(el,{pixelRatio:2});
    Object.assign(el.style, prev);

    const a = Object.assign(document.createElement('a'),{
        href:URL.createObjectURL(blob), download:name+'.png'});
    a.click();
}

/* ----- reset and fill map.* with new scenario ------------------ */
export function resetStateFromScenario(dto){
    lastScenario = dto;
    countersLbl.textContent =
        `T:${dto.terminals.length} V:${dto.ships.length} E:${dto.events.length}`;

    map.ships  = Object.fromEntries(dto.ships.map(s=>[s.id,s]));
    map.terms  = Object.fromEntries(dto.terminals.map(t=>[String(t.id),t]));
    map.events = dto.events;
}
/* ====== setters so that other modules can change the state ============== */
export function setLastPlan(p)        { lastPlan = p; }
export function setMLSchedule(arr)    { mlSchedule = arr; }
export function setManualSchedule(arr){ manualSchedule = arr; }
