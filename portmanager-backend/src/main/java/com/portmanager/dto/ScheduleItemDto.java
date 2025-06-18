package com.portmanager.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

/**
 * Single berth-occupation interval (terminal ↔ vessel).
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@Builder(toBuilder = true)
public class ScheduleItemDto {

    private Integer  terminalId;
    private String vesselId;
    private String startTime;   // ISO-8601
    private String endTime;     // ISO-8601
}
