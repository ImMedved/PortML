package com.portmanager.ui.controller;

import com.portmanager.ui.model.ScheduleItemDto;
import com.portmanager.ui.model.PlanResponseDto;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Region;
import lombok.Getter;

/**
 * Controls the plan table (terminal | interval).
 */
public class ScheduleController {

    @Getter
    @FXML private TableView<ScheduleRow> scheduleTable;
    @FXML private TableColumn<ScheduleRow, String> terminalColumn;
    @FXML private TableColumn<ScheduleRow, String> intervalColumn;

    /* we make the height of the TableView row fixed, after each redraw
        calculate the required pref/min/max height,
        so that VirtualFlow does not create a vertical ScrollBar */
    private static final double ROW_H = 26;      // px: one line
    private static final double HEADER_H = 28;   // px: header (~)

    private final ObservableList<ScheduleRow> rows = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        terminalColumn.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().terminal()));
        intervalColumn.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().interval()));
        scheduleTable.setItems(rows);
    }

    /** Called from AppController after plan is received. */
    public void renderPlan(PlanResponseDto dto) {
        rows.clear();
        for (ScheduleItemDto item : dto.getSchedule()) {
            rows.add(new ScheduleRow(
                    item.getTerminalId(),
                    item.getVesselId() + "  " +
                            item.getStartTime() + " – " + item.getEndTime()
            ));
        }
    }

    public record ScheduleRow(String terminal, String interval) {}

    /** Called by AppController every time after renderPlan() */
    public void fitHeightToRows() {
        scheduleTable.setFixedCellSize(26);
        scheduleTable.setPrefHeight(Region.USE_COMPUTED_SIZE);
        scheduleTable.setMinHeight(0);
        scheduleTable.setMaxHeight(Double.MAX_VALUE);

        if (scheduleTable.getParent() instanceof Region parent) {
            parent.setPrefHeight(Region.USE_COMPUTED_SIZE);
            parent.setMinHeight(0);
            parent.setMaxHeight(Double.MAX_VALUE);
        }
    }
}
