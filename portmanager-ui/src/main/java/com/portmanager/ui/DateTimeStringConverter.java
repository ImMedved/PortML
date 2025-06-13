package com.portmanager.ui;

import javafx.util.StringConverter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Converter for editable cells of TableView – translate
 * {@link LocalDateTime} ⇄ into string format  {@code yyyy-MM-dd HH:mm}.
 *
 * Text example for converter:  {@code 2025-06-14 08:30}
 */
public class DateTimeStringConverter extends StringConverter<LocalDateTime> {

    /** Shema without timezone. */
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public String toString(LocalDateTime value) {
        return value == null ? "" : value.format(FMT);
    }

    @Override
    public LocalDateTime fromString(String text) {
        if (text == null || text.isBlank()) return null;
        try {
            return LocalDateTime.parse(text.trim(), FMT);
        } catch (DateTimeParseException ex) {
            // incorrect format → null, for TableView not fall
            return null;
        }
    }
}
