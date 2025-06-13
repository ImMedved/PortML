package com.portmanager.ui.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class EventDto {

    public enum EventType { WEATHER, TERMINAL_CLOSURE }

    /** Serialize exactly as "eventType", tolerate old "type" on input */
    @JsonProperty("eventType")
    @JsonAlias("type")
    private EventType eventType;

    private LocalDateTime start;
    private LocalDateTime end;
    private String description;
    private String terminalId;

    public EventDto() {}

    public EventDto(EventType t, LocalDateTime s, LocalDateTime e, String d) {
        this.eventType = t;
        this.start = s;
        this.end = e;
        this.description = d;
    }

    /* ---------- getters / setters ---------- */
    public EventType getEventType()            { return eventType; }
    public void      setEventType(EventType t) { this.eventType = t; }

    public EventType getType()                 { return eventType; }
    public void      setType(EventType t)      { this.eventType = t; }

    public LocalDateTime getStart()            { return start; }
    public void         setStart(LocalDateTime s) { this.start = s; }

    public LocalDateTime getEnd()              { return end; }
    public void         setEnd(LocalDateTime e)   { this.end = e; }

    public String getDescription()             { return description; }
    public void   setDescription(String d)     { this.description = d; }

    public String getTerminalId()              { return terminalId; }
    public void   setTerminalId(String id)     { this.terminalId = id; }
}
