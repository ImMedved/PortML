package com.portmanager.ui.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class EventDto {

    public enum EventType { WEATHER, TERMINAL_CLOSURE }

    /* ---------- getters / setters ---------- */
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

    public EventType getType()                 { return eventType; }
    public void      setType(EventType t)      { this.eventType = t; }

}
