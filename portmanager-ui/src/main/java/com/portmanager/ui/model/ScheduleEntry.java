package com.portmanager.ui.model;

import java.time.LocalDateTime;

public class ScheduleEntry {
    private String shipId;
    private String terminalId;
    private LocalDateTime start;
    private LocalDateTime end;

    public ScheduleEntry() {}

    public ScheduleEntry(String shipId, String terminalId, LocalDateTime start, LocalDateTime end) {
        this.shipId = shipId;
        this.terminalId = terminalId;
        this.start = start;
        this.end = end;
    }

    public String getShipId() { return shipId; }
    public void setShipId(String shipId) { this.shipId = shipId; }

    public String getTerminalId() { return terminalId; }
    public void setTerminalId(String terminalId) { this.terminalId = terminalId; }

    public LocalDateTime getStart() { return start; }
    public void setStart(LocalDateTime start) { this.start = start; }

    public LocalDateTime getEnd() { return end; }
    public void setEnd(LocalDateTime end) { this.end = end; }
}
