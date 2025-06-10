package com.portmanager.ui.model;

import java.util.List;

public class Conditions {
    private List<TerminalClosure> terminalClosures;
    private List<WeatherEvent> weatherEvents;

    public List<TerminalClosure> getTerminalClosures() { return terminalClosures; }
    public List<WeatherEvent> getWeatherEvents() { return weatherEvents; }
}
