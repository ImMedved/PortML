package com.portmanager.ui.model;

import java.time.LocalDateTime;

public class EventDto {

    public enum EventType {
        WEATHER,
        TERMINAL_CLOSURE
    }

    private EventType type;
    private LocalDateTime start;
    private LocalDateTime end;
    private String terminalId;   // nullable for WEATHER
    private String description;

    public EventDto() {}

    public EventDto(EventType type, LocalDateTime start, LocalDateTime end, String terminalId, String description) {
        this.type = type;
        this.start = start;
        this.end = end;
        this.terminalId = terminalId;
        this.description = description;
    }

    public EventType getType() { return type; }
    public void setType(EventType type) { this.type = type; }

    public LocalDateTime getStart() { return start; }
    public void setStart(LocalDateTime start) { this.start = start; }

    public LocalDateTime getEnd() { return end; }
    public void setEnd(LocalDateTime end) { this.end = end; }

    public String getTerminalId() { return terminalId; }
    public void setTerminalId(String terminalId) { this.terminalId = terminalId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
