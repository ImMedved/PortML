package com.portmanager.ui.controller;

import com.portmanager.ui.model.EventDto;
import com.portmanager.ui.model.EventDto.EventType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EventsSettingsController implements SettingsResult<EventDto> {

    @FXML
    private TableView<EventDto> eventTable;
    @FXML
    private TableColumn<EventDto, EventType> typeColumn;
    @FXML
    private TableColumn<EventDto, LocalDateTime> startColumn;
    @FXML
    private TableColumn<EventDto, LocalDateTime> endColumn;
    @FXML
    private TableColumn<EventDto, String> descriptionColumn;

    private final ObservableList<EventDto> eventList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        startColumn.setCellValueFactory(new PropertyValueFactory<>("start"));
        endColumn.setCellValueFactory(new PropertyValueFactory<>("end"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        eventTable.setItems(eventList);
    }

    @FXML
    private void onAddEvent() {
        EventDto newEvent = new EventDto(EventType.WEATHER, LocalDateTime.now(), LocalDateTime.now().plusHours(1), "Weather disturbance");
        eventList.add(newEvent);
    }

    @FXML
    private void onConfirm() {
        ((Stage) eventTable.getScene().getWindow()).close();
    }

    @Override
    public List<EventDto> getData() {
        return List.of();
    }

    @Override
    public void setData(List<EventDto> data) {
        eventList.setAll(data);
    }

    @Override
    public List<EventDto> collectResult() {
        return new ArrayList<>(eventList);
    }
}
