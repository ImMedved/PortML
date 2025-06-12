package com.portmanager.dto;

import lombok.*;

/**
 * Single berth-occupation interval (terminal ↔ vessel).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ScheduleItemDto {

    private String terminalId;
    private String vesselId;
    private String startTime;   // ISO-8601
    private String endTime;     // ISO-8601
}
