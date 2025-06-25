package com.portmanager.ui.model;

import lombok.Getter;

@Getter
public class ShipRow {

    /* auxiliary access to the original dto */
    private final ShipDto dto;

    /* New convenient constructor  */
    public ShipRow(ShipDto dto) { this.dto = dto; }

    /* getters, guaranteed null-safe */
    private static String s(Object o) { return o == null ? "" : o.toString(); }

    public String getVesselId()   { return s(dto.getId()); }
    public String getArrivalTime(){ return s(dto.getArrivalTime()); }
    public String getCargoType()  { return s(dto.getCargoType()); }
    public String getPriority()   { return s(dto.getPriority()); }
    public String getLength()     { return s(dto.getLength()); }
    public String getDraft()      { return s(dto.getDraft()); }
    public String getDuration()   { return s(dto.getEstDurationHours()); }

}
