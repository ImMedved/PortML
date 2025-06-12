package com.portmanager.mapper;

import com.portmanager.dto.TerminalDto;
import com.portmanager.entity.TerminalEntity;
import org.springframework.stereotype.Component;

@Component
public class TerminalMapper {

    public TerminalEntity toEntity(TerminalDto dto) {
        TerminalEntity e = new TerminalEntity();
        e.setId(dto.id());              // может быть null → JPA сгенерирует
        e.setName(dto.name());
        e.setMaxLength(dto.maxLength());
        e.setMaxDraft(dto.maxDraft());
        e.setCargoTypes(dto.cargoTypes());
        return e;
    }

    public TerminalDto toDto(TerminalEntity e) {
        return new TerminalDto(
                e.getId(),
                e.getName(),
                e.getMaxLength(),
                e.getMaxDraft(),
                e.getCargoTypes()
        );
    }
}
