package com.portmanager.ui.model;

import java.util.Map;

public class ShipRow {
    private final ShipDto dto;

    public ShipRow(Map<String, Object> json) {
        this.dto = ShipDto.fromJson(json);
    }

    public ShipDto getDto() {
        return dto;
    }

    public String getVesselId() {
        return dto.getId();
    }

    public String getArrivalTime() {
        return dto.getArrivalTime();
    }

    public String getCargoType() {
        return dto.getCargoType();
    }

    public String getPriority() {
        return dto.getPriority();
    }

    public String getLength() {
        return String.valueOf(dto.getLength());
    }
}
