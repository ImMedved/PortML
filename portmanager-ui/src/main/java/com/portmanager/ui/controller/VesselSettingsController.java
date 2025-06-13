package com.portmanager.ui.controller;

import com.portmanager.ui.DateTimeStringConverter;
import com.portmanager.ui.cells.DateTimePickerTableCell;
import com.portmanager.ui.model.ShipDto;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DoubleStringConverter;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.util.List;

public class VesselSettingsController implements SettingsResult<ShipDto> {

    @FXML private TableView<ShipDto> shipTable;
    @FXML private TableColumn<ShipDto, String> idColumn;
    @FXML private TableColumn<ShipDto, Double> lengthColumn;
    @FXML private TableColumn<ShipDto, Double> draftColumn;
    @FXML private TableColumn<ShipDto, String> cargoColumn;
    @FXML private TableColumn<ShipDto, LocalDateTime> arrivalColumn;
    @FXML private TableColumn<ShipDto, String> durationColumn;

    private final ObservableList<ShipDto> shipList = FXCollections.observableArrayList();

    private static final List<String> CARGO_TYPES = List.of("containers", "vehicles", "gas", "liquid", "bulk");

    @FXML
    public void initialize() {
        shipTable.setEditable(true);

        idColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getId()));
        lengthColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getLength()));
        draftColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getDraft()));
        cargoColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getCargoType()));
        arrivalColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getArrival()));
        durationColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(String.valueOf(d.getValue().getEstDurationHours())));

        idColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        idColumn.setOnEditCommit(e -> e.getRowValue().setId(e.getNewValue()));

        lengthColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        lengthColumn.setOnEditCommit(e -> e.getRowValue().setLength(e.getNewValue()));

        draftColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        draftColumn.setOnEditCommit(e -> e.getRowValue().setDraft(e.getNewValue()));

        cargoColumn.setCellFactory(ComboBoxTableCell.forTableColumn(FXCollections.observableArrayList(CARGO_TYPES)));
        cargoColumn.setOnEditCommit(e -> e.getRowValue().setCargoType(e.getNewValue()));

        arrivalColumn.setCellFactory(col -> new DateTimePickerTableCell<>());
        arrivalColumn.setOnEditCommit(e -> e.getRowValue().setArrival(e.getNewValue()));

        durationColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        durationColumn.setOnEditCommit(e -> e.getRowValue().setEstDurationHours(Double.parseDouble(e.getNewValue())));

        shipTable.setItems(shipList);
    }

    @FXML
    private void onAddShip() {
        shipList.add(new ShipDto("S" + (shipList.size() + 1), LocalDateTime.now(), 150.0, 7.5, CARGO_TYPES.get(0), 10.0, "normal"));
    }

    @FXML
    private void onConfirm() {
        if (shipList.isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "There must be at least 1 vessel in the list").showAndWait();
            return;
        }
        ((Stage) shipTable.getScene().getWindow()).close();
    }

    @Override
    public List<ShipDto> getData() { return shipList; }

    @Override
    public void setData(List<ShipDto> data) { shipList.setAll(data); }

    @Override
    public List<ShipDto> collectResult() { return List.copyOf(shipList); }
}