package com.portmanager.ui.controller;

import com.portmanager.ui.model.ScheduleItemDto;
import com.portmanager.ui.model.PlanResponseDto;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * Controls the plan table (terminal | interval).
 */
public class ScheduleController {

    @FXML private TableView<ScheduleRow> scheduleTable;
    @FXML private TableColumn<ScheduleRow, String> terminalColumn;
    @FXML private TableColumn<ScheduleRow, String> intervalColumn;

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
}
