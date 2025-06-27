package com.portmanager.ui.model;

import lombok.Getter;

@Getter
public class ShipRow {

    private final ShipDto dto;

    public ShipRow(ShipDto d){ this.dto = d; }

    private static String s(Object o){ return o == null? "" : o.toString(); }

    public String getVesselId(){ return s(dto.getId()); }
    public String getArrivalTime(){ return s(dto.getArrivalTime()); }
    public String getCargoType(){ return s(dto.getCargoType()); }
    public String getShipType(){ return s(dto.getShipType()); }
    public String getPriority(){ return s(dto.getPriority()); }
    public String getLength(){ return s(dto.getLength()); }
    public String getDraft(){ return s(dto.getDraft()); }
    public String getDeadweight(){ return s(dto.getDeadweight()); }
    public String getDuration(){ return s(dto.getEstDurationHours()); }

    public String getFuelType(){ return s(dto.getFuelType()); }
    public String getEmissionRating(){ return s(dto.getEmissionRating()); }

    public String getFlagCountry(){ return s(dto.getFlagCountry()); }
    public String getImoNumber(){ return s(dto.getImoNumber()); }
    public String getRequiresCustomsClearance(){ return dto.isRequiresCustomsClearance() ? "✓" : ""; }
    public String getRequiresPilot(){ return dto.isRequiresPilot() ? "✓" : ""; }
    public String getTemperatureControlled(){ return dto.isTemperatureControlled()? "✓":""; }
    public String getHazardClass(){ return s(dto.getHazardClass()); }

    public String getArrivalPort(){ return s(dto.getArrivalPort()); }
    public String getNextPort(){ return s(dto.getNextPort()); }
    public String getArrivalWindowStart(){ return s(dto.getArrivalWindowStart()); }
    public String getArrivalWindowEnd(){ return s(dto.getArrivalWindowEnd()); }
    public String getExpectedDelayHours(){ return s(dto.getExpectedDelayHours()); }
}
