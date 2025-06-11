package com.portmanager.ui.controller;

import com.portmanager.ui.model.ConditionsDto;
import com.portmanager.ui.model.EventDto;
import com.portmanager.ui.model.PlanResponseDto;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.List;

public class ScheduleController {
    @FXML
    private TableView<ScheduleRow> scheduleTable;
    @FXML private TableColumn<ScheduleRow, String> terminalColumn;
    @FXML private TableColumn<ScheduleRow, String> timeColumn;

    private final ObservableList<ScheduleRow> rows = FXCollections.observableArrayList();

    public void initialize() {
        terminalColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().terminal()));
        timeColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().text()));
        scheduleTable.setItems(rows);
    }

    /**
     * Добавляем пустую строку‑заглушку после сохранения терминалов.Приходит размер списка терминалов. До получения плана остальные поля пустые.
     */
    public void renderConditions(ConditionsDto dto) {
        rows.clear();
        showTerminalCount(dto.terminals().size());
        markClosures(dto.events());
    }

    public void renderPlan(PlanResponseDto plan) {
        rows.clear();
        for (Object obj : plan.getSchedule()) {
            rows.add(new ScheduleRow("T", obj.toString())); // временно, заменить на реальные поля
        }
    }

    public void showTerminalCount(int count) {
        rows.add(new ScheduleRow("T=" + count, ""));
    }
    /** Подсветка эвентов закрытия. */
    public void markClosures(List<EventDto> events) {
        for (EventDto ev : events) {
            if (ev.getType() == EventDto.EventType.WEATHER || ev.getType() == EventDto.EventType.TERMINAL_CLOSURE) {
                ScheduleRow r = new ScheduleRow(
                        ev.getType() == EventDto.EventType.WEATHER ? "PORT" : ev.getTerminalId(),
                        ev.getStart() + " - " + ev.getEnd()
                );
                rows.add(r);
            }
        }
    }

    public record ScheduleRow(String terminal, String text) { /* style property через TableRow#setStyle */ }
}