package com.portmanager.ui.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * DTO describing a terminal.
 * Added “fuelSupported” to match vessel fuelType constraint.
 */
@Getter @Setter
public class TerminalDto {

    /* legacy */
    private int id;
    private String name;
    private double maxLength;
    private double maxDraft;
    private List<String> allowedCargoTypes;

    /* list of fuel types that can be bunkered here */
    private List<String> fuelSupported;

    public TerminalDto() {}

    public TerminalDto(int id,
                       String name,
                       double maxLength,
                       double maxDraft,
                       List<String> allowedCargoTypes) {
        this.id = id;
        this.name = name;
        this.maxLength = maxLength;
        this.maxDraft = maxDraft;
        this.allowedCargoTypes = allowedCargoTypes;
    }
}
