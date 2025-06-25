package com.portmanager.ui.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ScheduleItemDto {
    private String terminalId;
    private String vesselId;
    private String startTime;
    private String endTime;

}
