package com.portmanager.ui.controller;

import com.portmanager.ui.model.EventDto;
import com.portmanager.ui.model.EventDto.EventType;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.util.*;

public class EventsSettingsController {

    @FXML private TableView<EventDto> eventTable;
    @FXML private TableColumn<EventDto, EventType> typeColumn;
    @FXML private TableColumn<EventDto, LocalDateTime> startColumn;
    @FXML private TableColumn<EventDto, LocalDateTime> endColumn;
    @FXML private TableColumn<EventDto, String> terminalColumn;
    @FXML private TableColumn<EventDto, String> descriptionColumn;

    private final ObservableList<EventDto> events = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        eventTable.setItems(events);

        typeColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getType()));
        startColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getStart()));
        endColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getEnd()));
        terminalColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getTerminalId()));
        descriptionColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getDescription()));
    }

    @FXML
    private void onAddEvent() {
        EventDto event = new EventDto(
                EventType.WEATHER,
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(6),
                null,
                "Storm warning"
        );
        events.add(event);
    }

    @FXML
    private void onConfirm() {
        // TODO: validate, передать в основной контроллер
        ((Stage) eventTable.getScene().getWindow()).close();
    }

    public List<EventDto> getEvents() {
        return events;
    }

    public void setEvents(List<EventDto> initialData) {
        events.setAll(initialData);
    }
}
