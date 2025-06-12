package com.portmanager.ui.model;

public class ScheduleItemDto {
    private String terminalId;
    private String vesselId;
    private String startTime;
    private String endTime;

    public String getTerminalId() { return terminalId; }
    public void setTerminalId(String terminalId) { this.terminalId = terminalId; }

    public String getVesselId() { return vesselId; }
    public void setVesselId(String vesselId) { this.vesselId = vesselId; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
}
