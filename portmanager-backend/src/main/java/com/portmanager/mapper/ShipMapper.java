package com.portmanager.mapper;

import com.portmanager.dto.ShipDto;
import com.portmanager.entity.ShipEntity;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;

/**
 * Bidirectional mapper ShipDto ⇄ ShipEntity.
 */
@Component
public class ShipMapper {

    public ShipEntity toEntity(ShipDto dto) {
        ShipEntity e = new ShipEntity();
        e.setName(dto.getId());
        e.setLength(dto.getLength());
        e.setDraft(dto.getDraft());
        e.setCargoType(dto.getCargoType());

        /* LocalDateTime → UTC OffsetDateTime */
        e.setEta(dto.getArrivalTime().atOffset(ZoneOffset.UTC));

        e.setEstDurationHours(dto.getEstDurationHours());
        e.setPriority(dto.getPriority());
        return e;
    }

    public ShipDto toDto(ShipEntity e) {
        ShipDto dto = new ShipDto();
        dto.setId(e.getName());                               // UI-id
        dto.setLength(e.getLength());
        dto.setDraft(e.getDraft());
        dto.setCargoType(e.getCargoType());
        dto.setArrivalTime(e.getEta().toLocalDateTime());     // UTC → local
        dto.setEstDurationHours(e.getEstDurationHours());
        dto.setPriority(e.getPriority());
        return dto;
    }

}
