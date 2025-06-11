package com.portmanager.ui.controller;

import javafx.util.StringConverter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeStringConverter extends StringConverter<LocalDateTime> {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    @Override public String toString(LocalDateTime dt) { return dt == null ? "" : FMT.format(dt); }
    @Override public LocalDateTime fromString(String s) { return LocalDateTime.parse(s, FMT); }
}
