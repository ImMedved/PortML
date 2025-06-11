package com.portmanager.ui.controller;

import com.portmanager.ui.model.ShipDto;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.util.*;

public class VesselSettingsController {

    @FXML private TableView<ShipDto> vesselTable;
    @FXML private TableColumn<ShipDto, String> idColumn;
    @FXML private TableColumn<ShipDto, LocalDateTime> arrivalColumn;
    @FXML private TableColumn<ShipDto, Double> lengthColumn;
    @FXML private TableColumn<ShipDto, Double> draftColumn;
    @FXML private TableColumn<ShipDto, String> cargoColumn;
    @FXML private TableColumn<ShipDto, Double> durationColumn;
    @FXML private TableColumn<ShipDto, String> priorityColumn;

    private final ObservableList<ShipDto> vessels = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        vesselTable.setItems(vessels);

        idColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getId()));
        arrivalColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getArrivalTime()));
        lengthColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getLength()));
        draftColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getDraft()));
        cargoColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getCargoType()));
        durationColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getEstDurationHours()));
        priorityColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getPriority()));
    }

    @FXML
    private void onAddVessel() {
        ShipDto newShip = new ShipDto(
                UUID.randomUUID().toString().substring(0, 6),
                LocalDateTime.now().plusHours(new Random().nextInt(72)),
                100 + new Random().nextDouble() * 200,
                5 + new Random().nextDouble() * 5,
                "oil",
                6.0,
                "normal"
        );
        vessels.add(newShip);
    }

    @FXML
    private void onConfirm() {
        if (vessels.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "At least one vessel must be defined.");
            alert.showAndWait();
            return;
        }

        // TODO: сохранить vessels куда-то или передать в основной контроллер
        ((Stage) vesselTable.getScene().getWindow()).close();
    }

    public List<ShipDto> getVessels() {
        return vessels;
    }

    public void setVessels(List<ShipDto> initialData) {
        vessels.setAll(initialData);
    }
}
