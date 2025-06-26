package com.portmanager.mapper;

import com.portmanager.dto.ShipDto;
import com.portmanager.entity.ShipEntity;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;

/** Bidirectional mapper ShipDto ⇄ ShipEntity. */
@Component
public class ShipMapper {

    public ShipEntity toEntity(ShipDto d) {
        ShipEntity e = new ShipEntity();

        /* legacy */
        e.setName(d.getId());
        e.setLength(d.getLength());
        e.setDraft(d.getDraft());
        e.setCargoType(d.getCargoType());
        e.setEta(d.getArrivalTime().atOffset(ZoneOffset.UTC));
        e.setEstDurationHours(d.getEstDurationHours());
        e.setPriority(d.getPriority());

        /* new */
        e.setDeadweight(d.getDeadweight());
        e.setFlagCountry(d.getFlagCountry());
        e.setImoNumber(d.getImoNumber());
        e.setShipType(d.getShipType());
        e.setRequiresCustomsClearance(d.isRequiresCustomsClearance());
        e.setHazardClass(d.getHazardClass());
        e.setTemperatureControlled(d.isTemperatureControlled());
        e.setFuelType(d.getFuelType());
        e.setEmissionRating(d.getEmissionRating());
        e.setArrivalPort(d.getArrivalPort());
        e.setNextPort(d.getNextPort());
        e.setRequiresPilot(d.isRequiresPilot());
        e.setArrivalWindowStart(d.getArrivalWindowStart() == null ? null :
                d.getArrivalWindowStart().atOffset(ZoneOffset.UTC));
        e.setArrivalWindowEnd(d.getArrivalWindowEnd() == null ? null :
                d.getArrivalWindowEnd().atOffset(ZoneOffset.UTC));
        e.setExpectedDelayHours(d.getExpectedDelayHours());

        return e;
    }

    public ShipDto toDto(ShipEntity e) {
        ShipDto d = new ShipDto();

        /* legacy */
        d.setId(e.getName());
        d.setLength(e.getLength());
        d.setDraft(e.getDraft());
        d.setCargoType(e.getCargoType());
        d.setArrivalTime(e.getEta().toLocalDateTime());
        d.setEstDurationHours(e.getEstDurationHours());
        d.setPriority(e.getPriority());

        /* new */
        d.setDeadweight(e.getDeadweight());
        d.setFlagCountry(e.getFlagCountry());
        d.setImoNumber(e.getImoNumber());
        d.setShipType(e.getShipType());
        d.setRequiresCustomsClearance(e.isRequiresCustomsClearance());
        d.setHazardClass(e.getHazardClass());
        d.setTemperatureControlled(e.isTemperatureControlled());
        d.setFuelType(e.getFuelType());
        d.setEmissionRating(e.getEmissionRating());
        d.setArrivalPort(e.getArrivalPort());
        d.setNextPort(e.getNextPort());
        d.setRequiresPilot(e.isRequiresPilot());
        d.setArrivalWindowStart(e.getArrivalWindowStart() == null ? null :
                e.getArrivalWindowStart().toLocalDateTime());
        d.setArrivalWindowEnd(e.getArrivalWindowEnd() == null ? null :
                e.getArrivalWindowEnd().toLocalDateTime());
        d.setExpectedDelayHours(e.getExpectedDelayHours());

        return d;
    }
}
