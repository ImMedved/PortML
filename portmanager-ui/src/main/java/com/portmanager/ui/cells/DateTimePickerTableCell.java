package com.portmanager.ui.cells;

import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * TableCell with DatePicker + Spinner(hh:mm) for editing LocalDateTime.
 * <p>
 * Used like this:
 * <pre>
 * arrivalColumn.setCellFactory(col -> new DateTimePickerTableCell<>());
 * </pre>
 *
 * @param <S> string object type (ShipDto, EventDto …)
 */
public class DateTimePickerTableCell<S> extends TableCell<S, LocalDateTime> {

    /* ── graphics */
    private final DatePicker datePicker = new DatePicker();
    private final Spinner<Integer> hour = spinner(0, 23);
    private final Spinner<Integer> min  = spinner(0, 59);

    private final HBox editor = new HBox(4, datePicker, hour, new Label(":"), min);

    /* ── string format in "not editing" mode */
    private static final StringConverter<LocalDateTime> TEXT_FMT =
            new StringConverter<>() {
                @Override
                public String toString(LocalDateTime t) {
                    return t == null ? "" : t.toString().replace('T', ' ');
                }
                @Override
                public LocalDateTime fromString(String s) { return null; }
            };

    public DateTimePickerTableCell() {
        setPadding(new Insets(2));

        /* commit on ENTER */
        setOnKeyReleased(e -> {
            switch (e.getCode()) {
                case ENTER  -> commitEdit(picked());
                case ESCAPE -> cancelEdit();
            }
        });

        /* update the Spinner when selecting a date */
        ChangeListener<Object> h = (obs, o, n) -> {};        // empty - trigger only
        datePicker.valueProperty().addListener(h);
        hour.valueProperty().addListener(h);
        min.valueProperty().addListener(h);
    }

    /* TableCell API */

    @Override
    public void startEdit() {
        super.startEdit();
        if (!isEmpty()) {
            LocalDateTime val = getItem() == null ? LocalDateTime.now() : getItem();
            datePicker.setValue(val.toLocalDate());
            hour.getValueFactory().setValue(val.getHour());
            min.getValueFactory().setValue(val.getMinute());
        }
        setText(null);
        setGraphic(editor);
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        setGraphic(null);
        setText(TEXT_FMT.toString(getItem()));
    }

    @Override
    public void commitEdit(LocalDateTime newValue) {
        super.commitEdit(newValue);
        setGraphic(null);
        setText(TEXT_FMT.toString(newValue));
    }

    @Override
    protected void updateItem(LocalDateTime value, boolean empty) {
        super.updateItem(value, empty);
        if (empty) {
            setText(null);
            setGraphic(null);
        } else if (isEditing()) {
            setText(null);
            setGraphic(editor);
        } else {
            setText(TEXT_FMT.toString(value));
            setGraphic(null);
        }
    }

    /* ── helpers */

    private LocalDateTime picked() {
        LocalDate d = datePicker.getValue();
        if (d == null) d = LocalDate.now();
        return LocalDateTime.of(
                d,
                LocalTime.of(hour.getValue(), min.getValue())
        );
    }

    private static Spinner<Integer> spinner(int min, int max) {
        Spinner<Integer> sp = new Spinner<>(min, max, min);
        sp.setEditable(true);
        sp.setPrefWidth(60);
        sp.getValueFactory().setWrapAround(true);
        return sp;
    }
}
