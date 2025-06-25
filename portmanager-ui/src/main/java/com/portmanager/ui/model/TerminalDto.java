package com.portmanager.ui.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class TerminalDto {
    private int id;
    private String name;
    private double maxLength;
    private double maxDraft;
    private List<String> allowedCargoTypes;

    public TerminalDto() {}

    public TerminalDto(int id, String name, double maxLength, double maxDraft, List<String> allowedCargoTypes) {
        this.id = id;
        this.name = name;
        this.maxLength = maxLength;
        this.maxDraft = maxDraft;
        this.allowedCargoTypes = allowedCargoTypes;
    }

}
