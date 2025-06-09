package com.portmanager.ui.model;

import com.google.gson.annotations.SerializedName;

/**
 * One mooring interval.
 */
public class ScheduleEntry {

    @SerializedName("shipId")
    private String vesselId;

    @SerializedName("terminalId")
    private int terminalId;

    @SerializedName("startTime")
    private String startTime;

    @SerializedName("endTime")
    private String endTime;

    public String getVesselId() {
        return vesselId;
    }

    public int getTerminalId() {
        return terminalId;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }
}