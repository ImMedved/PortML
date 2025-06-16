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

/**
 * Controls the plan table (terminal | interval).
 */
public class ScheduleController {

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

    public TableView<ScheduleRow> getScheduleTable() {
        return scheduleTable;
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
        scheduleTable.setFixedCellSize(ROW_H);

        double h = HEADER_H + rows.size() * ROW_H;
        scheduleTable.setPrefHeight(h);
        scheduleTable.setMinHeight(h);
        scheduleTable.setMaxHeight(h);

        /* AnchorPane (parent) must also grow, otherwise VBox will consider the table “0 px” and still enable scroll */
        if (scheduleTable.getParent() instanceof Region parent) {
            parent.setPrefHeight(h);
            parent.setMinHeight(h);
            parent.setMaxHeight(h);
        }
    }
}
