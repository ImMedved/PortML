package com.portmanager.ui;

import com.portmanager.ui.controller.ScheduleController;
import com.portmanager.ui.controller.SettingsResult;
import com.portmanager.ui.model.*;
import com.portmanager.ui.service.BackendClient;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;

public class AppController {

    @FXML private ComboBox<String> algorithmSelector;
    @FXML private GridPane planGrid;
    @FXML private Label totalLabel, delayedLabel, utilLabel, planInfoLabel, statusLabel;
    @FXML private TableView<ShipRow> shipTable;
    @FXML private TableColumn<ShipRow, String> shipColumn, arrivalColumn, cargoColumn, priorityColumn, lengthColumn, draftColumn, durationColumn;
    @FXML private VBox conditionsBox;
    @FXML private Label weatherLabel, closuresLabel;
    @FXML private Label terminalCount, vesselCount, eventCount;
    @FXML private ScheduleController scheduleController;

    private final BackendClient backendClient = BackendClient.get();

    private List<ShipDto> manualShips = new ArrayList<>();
    private List<TerminalDto> manualTerminals = new ArrayList<>();
    private List<EventDto> manualEvents = new ArrayList<>();

    @FXML
    public void initialize() {
        algorithmSelector.getItems().addAll("baseline", "boosting", "RL");
        algorithmSelector.setValue("baseline");

        shipColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getVesselId()));
        arrivalColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getArrivalTime()));
        cargoColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCargoType()));
        priorityColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPriority()));
        lengthColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getLength()));
        draftColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDraft()));
        durationColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDuration()));

        shipTable.setOnMouseClicked(e -> Optional.ofNullable(shipTable.getSelectionModel().getSelectedItem())
                .ifPresent(s -> ShipInfoDialog.show(s.getDto())));
    }

    @FXML private void onGeneratePlan() {
        setStatus("Requesting plan...");
        ConditionsDto dto = new ConditionsDto(manualTerminals, manualShips, manualEvents);
        backendClient.generatePlan(dto).ifPresentOrElse(plan -> {
            scheduleController.renderPlan(plan);
            planInfoLabel.setText("ID: " + plan.getScenarioId() + " · " + plan.getAlgorithmUsed());
            setStatus("Ready");
        }, () -> {
            setStatus("Failed to get plan");
            showError("Error", "No plan received");
        });
    }

    @FXML private void onRandomData() {
        setStatus("Requesting random data...");
        backendClient.requestRandomData(20).ifPresentOrElse(dto -> {
            manualTerminals = dto.terminals();
            manualShips = dto.ships();
            manualEvents = dto.events();
            terminalCount.setText(String.valueOf(manualTerminals.size()));
            vesselCount.setText(String.valueOf(manualShips.size()));
            eventCount.setText(String.valueOf(manualEvents.size()));
            scheduleController.renderConditions(dto);
            setStatus("Random data loaded");
        }, () -> {
            setStatus("Failed to load random data");
            showError("Error", "No data received");
        });
    }

    @FXML private void onSaveData() {
        ConditionsDto dto = new ConditionsDto(manualTerminals, manualShips, manualEvents);
        boolean ok = backendClient.saveDataToDatabase(dto);
        if (ok) setStatus("Data saved");
        else setStatus("Save failed");
    }

    @FXML private void onShowLastPlan() {
        backendClient.getLastAcceptedPlan().ifPresentOrElse(plan -> {
            scheduleController.renderPlan(plan);
            planInfoLabel.setText("(cached) ID: " + plan.getScenarioId());
            setStatus("Plan displayed");
        }, () -> setStatus("No previous plan"));
    }

    @FXML private void openTerminalSettings() {
        manualTerminals = openSettingsDialog("/terminal_settings.fxml", manualTerminals);
        terminalCount.setText(String.valueOf(manualTerminals.size()));
        scheduleController.showTerminalCount(manualTerminals.size());
    }

    @FXML private void openVesselSettings() {
        manualShips = openSettingsDialog("/vessels_settings.fxml", manualShips);
        vesselCount.setText(String.valueOf(manualShips.size()));
    }

    @FXML private void openEventsSettings() {
        manualEvents = openSettingsDialog("/events_settings.fxml", manualEvents);
        eventCount.setText(String.valueOf(manualEvents.size()));
        scheduleController.markClosures(manualEvents);
    }

    private <T> List<T> openSettingsDialog(String fxmlPath, List<T> initial) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            SettingsResult<T> controller = loader.getController();
            controller.setData(initial);

            Stage dialog = new Stage();
            dialog.setTitle("Settings");
            dialog.setScene(new Scene(root));
            dialog.showAndWait();

            return controller.collectResult();
        } catch (IOException e) {
            e.printStackTrace();
            return initial;
        }
    }

    private void setStatus(String msg) {
        statusLabel.setText("[Port] " + msg);
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}