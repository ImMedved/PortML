package com.portmanager.ui.controller;

import com.portmanager.ui.DateTimeStringConverter;
import com.portmanager.ui.cells.DateTimePickerTableCell;
import com.portmanager.ui.model.EventDto;
import com.portmanager.ui.model.EventDto.EventType;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EventsSettingsController implements SettingsResult<EventDto> {

    @FXML private TableView<EventDto> eventTable;
    @FXML private TableColumn<EventDto, EventType> typeColumn;
    @FXML private TableColumn<EventDto, LocalDateTime> startColumn;
    @FXML private TableColumn<EventDto, LocalDateTime> endColumn;
    @FXML private TableColumn<EventDto, String> descriptionColumn;
    @FXML private TableColumn<EventDto, String> terminalIdColumn;

    private final ObservableList<EventDto> eventList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        eventTable.setEditable(true);

        typeColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getType()));
        startColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getStart()));
        endColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getEnd()));
        terminalIdColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getTerminalId()));
        descriptionColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getDescription()));

        typeColumn.setCellFactory(ComboBoxTableCell.forTableColumn(EventType.values()));
        typeColumn.setOnEditCommit(e -> e.getRowValue().setType(e.getNewValue()));

        descriptionColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        descriptionColumn.setOnEditCommit(e -> e.getRowValue().setDescription(e.getNewValue()));

        startColumn.setCellFactory(col -> new DateTimePickerTableCell<>());
        startColumn.setOnEditCommit(e -> e.getRowValue().setStart(e.getNewValue()));
        endColumn.setOnEditCommit(e -> e.getRowValue().setEnd(e.getNewValue()));
        terminalIdColumn.setOnEditCommit(e -> e.getRowValue().setTerminalId(e.getNewValue()));

        endColumn.setCellFactory(col -> new DateTimePickerTableCell<>());
        terminalIdColumn.setCellFactory(TextFieldTableCell.forTableColumn());

        eventTable.setItems(eventList);
    }

    @FXML
    private void onAddEvent() {
        eventList.add(new EventDto(EventType.WEATHER, LocalDateTime.now(), LocalDateTime.now().plusHours(1), "Weather"));
    }

    @FXML
    private void onConfirm() {
        ((Stage) eventTable.getScene().getWindow()).close();
    }

    @Override
    public List<EventDto> getData() { return eventList; }

    @Override
    public void setData(List<EventDto> data) { eventList.setAll(data); }

    @Override
    public List<EventDto> collectResult() { return new ArrayList<>(eventList); }
}