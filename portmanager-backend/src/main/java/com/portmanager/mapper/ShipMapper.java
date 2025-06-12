package com.portmanager.mapper;

import com.portmanager.dto.ShipDto;
import com.portmanager.entity.ShipEntity;
import org.springframework.stereotype.Component;

/**
 * Bidirectional mapper ShipDto ⇄ ShipEntity.
 */
@Component
public class ShipMapper {

    public ShipEntity toEntity(ShipDto dto) {
        ShipEntity e = new ShipEntity();
        e.setId(dto.id());
        e.setName(dto.name());
        e.setLength(dto.length());
        e.setDraft(dto.draft());
        e.setCargoType(dto.cargoType());
        e.setEta(dto.eta());
        e.setEstDurationHours(dto.estDurationHours());
        e.setPriority(dto.priority());
        return e;
    }

    public ShipDto toDto(ShipEntity e) {
        return new ShipDto(
                e.getId(),
                e.getName(),
                e.getLength(),
                e.getDraft(),
                e.getCargoType(),
                e.getEta(),
                e.getEstDurationHours(),
                e.getPriority()
        );
    }
}
