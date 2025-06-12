package com.portmanager.ui;

import com.portmanager.ui.board.ManualBoardController;
import com.portmanager.ui.board.MlBoardController;
import com.portmanager.ui.model.*;
import com.portmanager.ui.service.BackendClient;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;

/**
 * Main controller that wires UI together.
 * Owns two boards: manual (editable) and ML (read-only).
 */
public class AppController {

    /* ---------- FXML ---------- */
    @FXML private ComboBox<String> algorithmSelector;
    @FXML private GridPane manualGrid;
    @FXML private GridPane mlGrid;

    @FXML private Label planInfoLabel, statusLabel;
    @FXML private Label terminalCount, vesselCount, eventCount;

    @FXML private TableView<ShipRow> shipTable;
    @FXML private TableColumn<ShipRow, String> shipColumn, arrivalColumn,
            cargoColumn, priorityColumn, lengthColumn, draftColumn, durationColumn;

    /* ---------- state ---------- */
    private final BackendClient backend = BackendClient.get();
    private final ManualBoardController manualBoard = new ManualBoardController();
    private final MlBoardController mlBoard       = new MlBoardController();

    private List<ShipDto>     ships     = new ArrayList<>();
    private List<TerminalDto> terminals = new ArrayList<>();
    private List<EventDto>    events    = new ArrayList<>();

    /* ---------- init ---------- */
    @FXML
    private void initialize() {
        algorithmSelector.getItems().addAll("baseline", "boosting", "RL");
        algorithmSelector.setValue("baseline");

        bindShipTable();
        manualBoard.attach(manualGrid);
        mlBoard.attach(mlGrid);
    }

    /* ---------- toolbar actions ---------- */

    @FXML
    private void onGeneratePlan() {
        setStatus("Requesting plan …");
        ConditionsDto dto = new ConditionsDto(terminals, ships, events);

        backend.generatePlan(dto).ifPresentOrElse(plan -> {
            mlBoard.renderConditions(dto);
            mlBoard.renderPlan(plan);
            planInfoLabel.setText("ID: %s · %s".formatted(plan.getScenarioId(), plan.getAlgorithmUsed()));
            setStatus("Plan received");
        }, () -> setStatus("Failed to get plan"));
    }

    @FXML
    private void onRandomData() {
        setStatus("Requesting random data …");
        backend.requestRandomData(20).ifPresentOrElse(dto -> {
            terminals = dto.terminals();
            ships     = dto.ships();
            events    = dto.events();
            refreshCounts();
            reloadBoards();
            setStatus("Random data loaded");
        }, () -> setStatus("Failed to load data"));
    }

    @FXML
    private void onSaveData() {
        boolean ok = backend.saveDataToDatabase(new ConditionsDto(terminals, ships, events));
        setStatus(ok ? "Data saved" : "Save failed");
    }

    /* ---------- settings dialogs ---------- */

    @FXML private void openTerminalSettings() { terminals = open("/terminal_settings.fxml", terminals);  afterEdit(); }
    @FXML private void openVesselSettings()   { ships     = open("/vessels_settings.fxml",  ships);      afterEdit(); }
    @FXML private void openEventsSettings()   { events    = open("/events_settings.fxml",   events);     afterEdit(); }

    /* ---------- helpers ---------- */

    private <T> List<T> open(String fxml, List<T> init) {
        try {
            FXMLLoader l = new FXMLLoader(getClass().getResource(fxml));
            Parent root  = l.load();
            var ctrl     = (com.portmanager.ui.controller.SettingsResult<T>) l.getController();
            ctrl.setData(init);

            Stage dlg = new Stage();
            dlg.setTitle("Settings");
            dlg.setScene(new Scene(root));
            dlg.showAndWait();
            return ctrl.collectResult();
        } catch (IOException ex) {
            ex.printStackTrace();
            return init;
        }
    }

    private void afterEdit() {
        refreshCounts();
        reloadBoards();
    }

    private void reloadBoards() {
        ConditionsDto dto = new ConditionsDto(terminals, ships, events);
        manualBoard.renderConditions(dto);
        mlBoard.renderConditions(dto);   // only rows + events
    }

    private void refreshCounts() {
        terminalCount.setText(String.valueOf(terminals.size()));
        vesselCount.setText(String.valueOf(ships.size()));
        eventCount.setText(String.valueOf(events.size()));
    }

    private void setStatus(String msg) { statusLabel.setText("[Port] " + msg); }

    private void bindShipTable() {
        shipColumn.setCellValueFactory   (d -> new SimpleStringProperty(d.getValue().getVesselId()));
        arrivalColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getArrivalTime()));
        cargoColumn.setCellValueFactory  (d -> new SimpleStringProperty(d.getValue().getCargoType()));
        priorityColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPriority()));
        lengthColumn.setCellValueFactory (d -> new SimpleStringProperty(d.getValue().getLength()));
        draftColumn.setCellValueFactory  (d -> new SimpleStringProperty(d.getValue().getDraft()));
        durationColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDuration()));

        shipTable.setOnMouseClicked(e ->
                Optional.ofNullable(shipTable.getSelectionModel().getSelectedItem())
                        .ifPresent(s -> ShipInfoDialog.show(s.getDto()))
        );
    }
}
