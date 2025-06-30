package com.portmanager.ui.controller;

import com.portmanager.ui.cells.DateTimePickerTableCell;
import com.portmanager.ui.model.ShipDto;
import com.portmanager.ui.model.TerminalDto;
import com.portmanager.ui.service.BackendClient;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.NumberStringConverter;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.util.List;

public class VesselSettingsController implements SettingsResult<ShipDto> {

    /* ---------- FXML ---------- */
    @FXML private TableView<ShipDto> shipTable;

    @FXML private TableColumn<ShipDto,String> idColumn, cargoColumn, shipTypeColumn,
            flagColumn, imoColumn, fuelColumn, emissionColumn,
            arrivalPortColumn, nextPortColumn, hazardColumn, priorityColumn;

    @FXML private TableColumn<ShipDto,Number> deadweightColumn, durationColumn, delayColumn;
    @FXML private TableColumn<ShipDto,Double> lengthColumn, draftColumn;

    @FXML private TableColumn<ShipDto,Boolean> customsColumn, pilotColumn, tempColumn;

    @FXML private TableColumn<ShipDto,LocalDateTime> arrivalColumn,
            windowStartColumn, windowEndColumn;

    /* ---------- data ---------- */
    private final ObservableList<ShipDto> shipList = FXCollections.observableArrayList();
    private static final List<String> CARGO_TYPES =
            List.of("container","general","bulk","oil","lng");
    private static final List<String> SHIP_TYPES  = CARGO_TYPES;
    private static final List<String> FUEL_TYPES  = List.of("diesel","lng");
    private static final List<String> EMISSIONS   = List.of("A","B","C");
    private static final List<String> PRIORITIES  = List.of("normal","high");

    @FXML
    public void initialize(){

        /* ---------- cell value factories ---------- */
        idColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getId()));
        cargoColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getCargoType()));
        shipTypeColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getShipType()));
        flagColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getFlagCountry()));
        imoColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getImoNumber()));
        fuelColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getFuelType()));
        emissionColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getEmissionRating()));
        arrivalPortColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getArrivalPort()));
        nextPortColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getNextPort()));
        hazardColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getHazardClass()));
        priorityColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getPriority()));

        lengthColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getLength()));
        draftColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getDraft()));
        deadweightColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getDeadweight()));
        durationColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getEstDurationHours()));
        delayColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getExpectedDelayHours()));

        customsColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().isRequiresCustomsClearance()));
        pilotColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().isRequiresPilot()));
        tempColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().isTemperatureControlled()));

        arrivalColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getArrivalTime()));
        windowStartColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getArrivalWindowStart()));
        windowEndColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getArrivalWindowEnd()));

        /* ---------- editors ---------- */
        idColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        idColumn.setOnEditCommit(e -> e.getRowValue().setId(e.getNewValue()));

        cargoColumn.setCellFactory(ComboBoxTableCell.forTableColumn(FXCollections.observableArrayList(CARGO_TYPES)));
        cargoColumn.setOnEditCommit(e -> e.getRowValue().setCargoType(e.getNewValue()));

        shipTypeColumn.setCellFactory(ComboBoxTableCell.forTableColumn(FXCollections.observableArrayList(SHIP_TYPES)));
        shipTypeColumn.setOnEditCommit(e -> e.getRowValue().setShipType(e.getNewValue()));

        fuelColumn.setCellFactory(ComboBoxTableCell.forTableColumn(FXCollections.observableArrayList(FUEL_TYPES)));
        fuelColumn.setOnEditCommit(e -> e.getRowValue().setFuelType(e.getNewValue()));

        emissionColumn.setCellFactory(ComboBoxTableCell.forTableColumn(FXCollections.observableArrayList(EMISSIONS)));
        emissionColumn.setOnEditCommit(e -> e.getRowValue().setEmissionRating(e.getNewValue()));

        priorityColumn.setCellFactory(ComboBoxTableCell.forTableColumn(FXCollections.observableArrayList(PRIORITIES)));
        priorityColumn.setOnEditCommit(e -> e.getRowValue().setPriority(e.getNewValue()));

        lengthColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        lengthColumn.setOnEditCommit(e -> {
            e.getRowValue().setLength(e.getNewValue().doubleValue());
        });
        draftColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        draftColumn.setOnEditCommit(e -> {
            e.getRowValue().setDraft(e.getNewValue().doubleValue());
        });
        deadweightColumn.setCellFactory(TextFieldTableCell.forTableColumn(new NumberStringConverter()));
        deadweightColumn.setOnEditCommit(e -> e.getRowValue().setDeadweight(e.getNewValue().doubleValue()));

        durationColumn.setCellFactory(TextFieldTableCell.forTableColumn(new NumberStringConverter()));
        durationColumn.setOnEditCommit(e -> e.getRowValue().setEstDurationHours(e.getNewValue().doubleValue()));

        delayColumn.setCellFactory(TextFieldTableCell.forTableColumn(new NumberStringConverter()));
        delayColumn.setOnEditCommit(e -> e.getRowValue().setExpectedDelayHours(e.getNewValue().doubleValue()));

        customsColumn.setCellFactory(CheckBoxTableCell.forTableColumn(customsColumn));
        customsColumn.setEditable(true);
        customsColumn.setOnEditCommit(e -> e.getRowValue().setRequiresCustomsClearance(e.getNewValue()));

        pilotColumn.setCellFactory(CheckBoxTableCell.forTableColumn(pilotColumn));
        pilotColumn.setEditable(true);
        pilotColumn.setOnEditCommit(e -> {
            e.getRowValue().setRequiresPilot(e.getNewValue());
            // TODO: If a pilot is required, add a default delay after service (e.g. set some delay hours).
        });

        tempColumn.setCellFactory(CheckBoxTableCell.forTableColumn(tempColumn));
        tempColumn.setEditable(true);
        tempColumn.setOnEditCommit(e -> e.getRowValue().setTemperatureControlled(e.getNewValue()));

        hazardColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        hazardColumn.setOnEditCommit(e -> e.getRowValue().setHazardClass(e.getNewValue()));

        flagColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        flagColumn.setOnEditCommit(e -> e.getRowValue().setFlagCountry(e.getNewValue()));

        imoColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        imoColumn.setOnEditCommit(e -> e.getRowValue().setImoNumber(e.getNewValue()));

        arrivalPortColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        arrivalPortColumn.setOnEditCommit(e -> e.getRowValue().setArrivalPort(e.getNewValue()));

        nextPortColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nextPortColumn.setOnEditCommit(e -> e.getRowValue().setNextPort(e.getNewValue()));

        arrivalColumn.setCellFactory(col -> new DateTimePickerTableCell<>());
        arrivalColumn.setOnEditCommit(e -> e.getRowValue().setArrivalTime(e.getNewValue()));

        windowStartColumn.setCellFactory(col -> new DateTimePickerTableCell<>());
        windowStartColumn.setOnEditCommit(e -> e.getRowValue().setArrivalWindowStart(e.getNewValue()));

        windowEndColumn.setCellFactory(col -> new DateTimePickerTableCell<>());
        windowEndColumn.setOnEditCommit(e -> e.getRowValue().setArrivalWindowEnd(e.getNewValue()));

        shipTable.setEditable(true);
        shipTable.setItems(shipList);
    }

    /* ---------- toolbar ---------- */
    @FXML private void onAddShip(){
        ShipDto s = new ShipDto();
        s.setId("V" + (shipList.size()+1));
        s.setArrivalTime(LocalDateTime.now());
        s.setLength(150); s.setDraft(7);
        s.setCargoType(CARGO_TYPES.get(0));
        s.setShipType(s.getCargoType());
        s.setFuelType(FUEL_TYPES.get(0));
        s.setEstDurationHours(8);
        shipList.add(s);
    }
    @FXML private void onConfirm(){
        ((Stage) shipTable.getScene().getWindow()).close();
    }

    /* ---------- interface ---------- */
    @Override public List<ShipDto> getData(){ return shipList; }
    @Override public void setData(List<ShipDto> data){ shipList.setAll(data); }
    @Override public List<ShipDto> collectResult(){ return List.copyOf(shipList); }

    @FXML private void onDeleteShip() {
        long numericId;
        ShipDto sel = shipTable.getSelectionModel().getSelectedItem();
        try {
            numericId = Long.parseLong(sel.getId().replaceAll("\\D", ""));
        } catch (NumberFormatException ex) {
            showError("Incorrect vessel ID: " + sel.getId());
            return;
        }

        if (BackendClient.get().deleteShip(numericId)) {
            shipList.remove(sel);
        } else {
            showError("Unsuccess to delete vessel: " + sel.getId());
        }
    }

    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait();
    }
}
