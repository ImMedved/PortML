package com.portmanager.ui;

import javafx.util.StringConverter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Конвертер для редактируемых ячеек TableView – переводит
 * {@link LocalDateTime} ⇄ строку формата  {@code yyyy-MM-dd HH:mm}.
 *
 * Пример текста, который «понимает» конвертер:  {@code 2025-06-14 08:30}
 */
public class DateTimeStringConverter extends StringConverter<LocalDateTime> {

    /** Универсальный шаблон без часового пояса. */
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
            // неверный формат → null, чтобы TableView не падал
            return null;
        }
    }
}
