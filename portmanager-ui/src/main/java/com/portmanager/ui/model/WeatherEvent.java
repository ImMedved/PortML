package com.portmanager.ui.model;

public class WeatherEvent {
    private String start;
    private String end;
    private String description;

    public String getStart() { return start; }
    public String getEnd() { return end; }
    public String getDescription() { return description; }

    public Object getType() {
        return description;
    }
}
