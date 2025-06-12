package com.portmanager.ui.model;

import java.util.List;

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

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getMaxLength() { return maxLength; }
    public void setMaxLength(double maxLength) { this.maxLength = maxLength; }

    public double getMaxDraft() { return maxDraft; }
    public void setMaxDraft(double maxDraft) { this.maxDraft = maxDraft; }

    public List<String> getAllowedCargoTypes() { return allowedCargoTypes; }
    public void setAllowedCargoTypes(List<String> allowedCargoTypes) { this.allowedCargoTypes = allowedCargoTypes; }
}
