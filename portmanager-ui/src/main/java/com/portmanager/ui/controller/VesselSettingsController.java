package com.portmanager.ui.controller;

import com.portmanager.ui.model.ShipDto;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.util.List;

public class VesselSettingsController implements SettingsResult<ShipDto> {

    @FXML
    private TableView<ShipDto> shipTable;
    @FXML
    private TableColumn<ShipDto, String> idColumn;
    @FXML
    private TableColumn<ShipDto, String> nameColumn;
    @FXML
    private TableColumn<ShipDto, Integer> lengthColumn;
    @FXML
    private TableColumn<ShipDto, Integer> draftColumn;
    @FXML
    private TableColumn<ShipDto, String> cargoColumn;

    private final ObservableList<ShipDto> shipList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        lengthColumn.setCellValueFactory(new PropertyValueFactory<>("length"));
        draftColumn.setCellValueFactory(new PropertyValueFactory<>("draft"));
        cargoColumn.setCellValueFactory(new PropertyValueFactory<>("cargoType"));

        shipTable.setItems(shipList);
    }

    @FXML
    private void onAddShip() {
        ShipDto newShip = new ShipDto(
                "S" + (shipList.size() + 1),
                LocalDateTime.now(),
                150.0,
                7.5,
                "GENERAL",
                10.0,
                "normal"
        );
        shipList.add(newShip);
    }

    @FXML
    private void onConfirm() {
        ((Stage) shipTable.getScene().getWindow()).close();
    }

    @Override
    public List<ShipDto> getData() {
        return List.of();
    }

    @Override
    public void setData(List<ShipDto> data) {

    }

    @Override
    public List<ShipDto> collectResult() {
        return shipList;
    }
}