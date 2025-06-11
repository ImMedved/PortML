package com.portmanager.ui;

import com.portmanager.ui.controller.*;
import com.portmanager.ui.model.*;
import com.portmanager.ui.net.RestClient;
import com.portmanager.ui.service.BackendClient;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;

public class AppController {

    private final RestClient api = new RestClient("http://localhost:8080/api");

    @FXML private ComboBox<String> algorithmSelector;
    @FXML private GridPane planGrid;
    @FXML private Label totalLabel, delayedLabel, utilLabel, planInfoLabel, statusLabel;
    @FXML private TableView<ShipRow> shipTable;
    @FXML private TableColumn<ShipRow, String> shipColumn, arrivalColumn, cargoColumn, priorityColumn, lengthColumn, draftColumn, durationColumn;
    @FXML private VBox conditionsBox;
    @FXML private Label weatherLabel, closuresLabel;
    @FXML private Label terminalCount, vesselCount, eventCount;
    @FXML private ScheduleController scheduleController;

    private final BackendClient backendClient = new BackendClient();
    private PlanResponse lastPlan;

    private List<ShipDto> manualShips = new ArrayList<>();
    private List<TerminalDto> manualTerminals = new ArrayList<>();
    private List<EventDto> manualEvents = new ArrayList<>();

    @FXML
    public void initialize() {
        algorithmSelector.getItems().addAll("baseline", "boosting", "RL");
        algorithmSelector.setValue("baseline");

        shipColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getVesselId()));
        arrivalColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getArrivalTime()));
        cargoColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCargoType()));
        priorityColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPriority()));
        lengthColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLength()));
        draftColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDraft()));
        durationColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDuration()));

        shipTable.setOnMouseClicked(event -> {
            ShipRow selected = shipTable.getSelectionModel().getSelectedItem();
            if (selected != null) ShipInfoDialog.show(selected.getDto());
        });
    }

    @FXML
    private void onComparePlans() {
        statusLabel.setText("Compare plans not implemented yet.");
    }

    @FXML
    private void onRejectPlan() {
        statusLabel.setText("Compare plans not implemented yet.");
    }

    @FXML
    private void onAcceptPlan() {
        statusLabel.setText("Compare plans not implemented yet.");
    }

    @FXML
    private void onChooseA() {
        statusLabel.setText("Compare plans not implemented yet.");
    }

    @FXML
    private void onChooseB() {
        statusLabel.setText("Compare plans not implemented yet.");
    }

    @FXML
    private void onGeneratePlan() {
        ConditionsDto dto = new ConditionsDto(manualTerminals, manualShips, manualEvents);
        PlanResponseDto plan = api.post("/plan", dto, PlanResponseDto.class);
        scheduleController.renderPlan(plan);
    }

    @FXML
    private void onRandomData() {
        api.post("/data/generate?ships=20", null, Void.class);
        ConditionsDto dto = api.get("/conditions", ConditionsDto.class);
        manualTerminals = dto.terminals();
        manualShips = dto.ships();
        manualEvents = dto.events();
        scheduleController.renderConditions(dto);
    }

    @FXML
    private void onRefreshPlan() {
        setStatus("Getting a plan...");
        planGrid.getChildren().clear();
        planInfoLabel.setText("ID: -, Algorithm: -");

        Optional<PlanResponse> response = backendClient.generatePlan(algorithmSelector.getValue());
        if (response.isPresent()) {
            lastPlan = response.get();
            renderPlan(lastPlan);
            planInfoLabel.setText("Scenario #" + lastPlan.getScenarioId() + " · Algorithm: " + lastPlan.getAlgorithmUsed());
            setStatus("Ready");
        } else {
            showError("Error", "Failed to get plan from server.");
            setStatus("Error while getting plan.");
        }
    }

    @FXML
    private void openTerminalSettings() {
        manualTerminals = openSettingsDialog("/terminal_settings.fxml", manualTerminals);
        terminalCount.setText(String.valueOf(manualTerminals.size()));
        scheduleController.showTerminalCount(manualTerminals.size());
    }

    @FXML
    private void openVesselSettings() {
        manualShips = openSettingsDialog("/vessels_settings.fxml", manualShips);
        vesselCount.setText(String.valueOf(manualShips.size()));
    }

    @FXML
    private void openEventsSettings() {
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

    private <T> List<T> openSettingsDialog(String fxmlPath) {
        return openSettingsDialog(fxmlPath, List.of());
    }

    private void renderPlan(PlanResponse plan) {
        // render logic
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