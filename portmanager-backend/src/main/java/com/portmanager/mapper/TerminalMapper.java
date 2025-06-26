package com.portmanager.mapper;

import com.portmanager.dto.TerminalDto;
import com.portmanager.entity.TerminalEntity;
import org.springframework.stereotype.Component;

/** Bidirectional mapper TerminalDto ⇄ TerminalEntity. */
@Component
public class TerminalMapper {
    public TerminalEntity toEntity(TerminalDto dto) {
        TerminalEntity e = new TerminalEntity();
        e.setId(dto.id());                      // was dto.getId()
        e.setName(dto.name());                  // was dto.getName()
        e.setMaxLength(dto.maxLength());
        e.setMaxDraft(dto.maxDraft());
        e.setCargoTypes(dto.cargoTypes());
        e.setFuelSupported(dto.fuelSupported());  // new field
        return e;
    }

    public TerminalDto toDto(TerminalEntity e) {
        return new TerminalDto(
                e.getId(),
                e.getName(),
                e.getMaxLength(),
                e.getMaxDraft(),
                e.getCargoTypes(),
                e.getFuelSupported()
        );
    }
}
