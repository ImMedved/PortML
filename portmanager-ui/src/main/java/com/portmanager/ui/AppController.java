package com.portmanager.ui;

import com.portmanager.ui.model.*;
import com.portmanager.ui.service.BackendClient;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * JavaFX screen controller. Contains onAcceptPlan/onRejectPlan stubs
 */
public class AppController {

    private boolean mlGenerated;

    @FXML private ComboBox<String> cargoSelector;
    @FXML private ComboBox<String> prioritySelector;
    @FXML private CheckBox disableT1;
    @FXML private CheckBox disableT2;

    @FXML private Label totalLabel;
    @FXML private Label delayedLabel;
    @FXML private Label utilLabel;

    @FXML private Label aTotal;
    @FXML private Label aDelayed;
    @FXML private Label aUtil;

    @FXML private Label bTotal;
    @FXML private Label bDelayed;
    @FXML private Label bUtil;

    @FXML private ComboBox<String> algorithmSelector;
    @FXML private TabPane scheduleTabs;
    @FXML private Label statusLabel;

    @FXML private GridPane planGrid;
    @FXML private GridPane planAGrid;
    @FXML private GridPane planBGrid;

    private PairwiseRequest currentPair;
    private final BackendClient backendClient = new BackendClient();

    @FXML private Button acceptButton;
    @FXML private Button rejectButton;
    @FXML private Label planInfoLabel;

    @FXML private TableView<ShipRow> shipTable;
    @FXML private TableColumn<ShipRow, String> shipColumn;
    @FXML private TableColumn<ShipRow, String> arrivalColumn;

    @FXML private TableColumn<ShipRow, String> cargoColumn;
    @FXML private TableColumn<ShipRow, String> priorityColumn;
    @FXML private TableColumn<ShipRow, String> lengthColumn;

    private PlanResponse lastSinglePlan;
    // Stubs - required only for correct parsing of ui.fxml
    @FXML
    private void onAcceptPlan() {
        // Demo
        statusLabel.setText("The plan has been adopted (no‑op)");
    }

    @FXML
    private void onRejectPlan() {
        statusLabel.setText("The plan was rejected (no‑op)");
    }

    public boolean isMlGenerated() {
        return mlGenerated;
    }

    @FXML
    public void initialize() {
        algorithmSelector.getItems().addAll("baseline", "boosting", "RL");
        algorithmSelector.setValue("baseline");

        cargoSelector.getItems().addAll("bulk", "liquid", "containers");
        cargoSelector.setValue("bulk");

        prioritySelector.getItems().addAll("normal", "high", "critical");
        prioritySelector.setValue("normal");

        shipColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getVesselId()));
        arrivalColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getArrivalTime()));

        cargoColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCargoType()));
        priorityColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPriority()));
        lengthColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLength()));

        shipTable.setOnMouseClicked(event -> {
            ShipRow selected = shipTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                ShipInfoDialog.show(selected.getDto());
            }
        });
    }

    @FXML
    private void onRefreshPlan() {
        setStatus("Getting a plan...");
        planGrid.getChildren().clear();
        planInfoLabel.setText("ID: -, Algorithm: -");
        acceptButton.setVisible(false);
        rejectButton.setVisible(false);

        Optional<PlanResponse> response = backendClient.generatePlan(algorithmSelector.getValue());
        if (response.isPresent()) {
            lastSinglePlan = response.get();
            renderPlan(lastSinglePlan);
            renderMetrics(lastSinglePlan.getMetrics(), totalLabel, delayedLabel, utilLabel);
            planInfoLabel.setText("Scenario #" + lastSinglePlan.getScenarioId() +
                    " \u00B7 Algorithm: " + lastSinglePlan.getAlgorithmUsed());
            setStatus("Ready");
        } else {
            showError("Error", "Failed to get plan from server.");
            setStatus("Error while getting plan.");
        }
    }

    @FXML
    private void onComparePlans() {
        planAGrid.getChildren().clear();
        planBGrid.getChildren().clear();
        aTotal.setText("Всего: -");
        bTotal.setText("Всего: -");

        Optional<PairwiseRequest> response = backendClient.getPairwisePlans();
        if (response.isPresent()) {
            currentPair = response.get();
            renderPlanToGrid(currentPair.getPlanA(), planAGrid);
            renderPlanToGrid(currentPair.getPlanB(), planBGrid);
            renderMetrics(currentPair.getPlanA().getMetrics(), aTotal, aDelayed, aUtil);
            renderMetrics(currentPair.getPlanB().getMetrics(), bTotal, bDelayed, bUtil);
            scheduleTabs.getSelectionModel().select(1);
            statusLabel.setText("Plans loaded");
        } else {
            statusLabel.setText("Error in loading plans");
        }
    }

    @FXML
    private void onChooseA() {
        if (currentPair != null) {
            backendClient.sendFeedback(currentPair.getComparisonId(), "A");
            statusLabel.setText("Plan A chosen");
        }
    }

    @FXML
    private void onChooseB() {
        if (currentPair != null) {
            backendClient.sendFeedback(currentPair.getComparisonId(), "B");
            statusLabel.setText("Plan B chosen");
        }
    }

    private void renderPlan(PlanResponse plan) {

        if (plan.getSchedule() == null || plan.getSchedule().isEmpty()) {
            showError("Empty plan", "ML-service returned empty plan.");
            return;
        }

        planGrid.getChildren().clear();

        // Gather terminals and timestamps
        Set<Integer> terminals = new TreeSet<>();
        Set<OffsetDateTime> timeline = new TreeSet<>();

        for (ScheduleEntry e : plan.getSchedule()) {
            terminals.add(e.getTerminalId());
            timeline.add(OffsetDateTime.parse(e.getStartTime(), DateTimeFormatter.ISO_OFFSET_DATE_TIME));
            timeline.add(OffsetDateTime.parse(e.getEndTime(), DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        }

        List<Integer> termList = new ArrayList<>(terminals);
        List<OffsetDateTime> tLine = new ArrayList<>(timeline);

        // Time heading
        for (int col = 0; col < tLine.size(); col++) {
            planGrid.add(new Label(tLine.get(col).toLocalTime().toString()), col + 1, 0);
        }
        // Terminals heading
        for (int row = 0; row < termList.size(); row++) {
            planGrid.add(new Label(String.valueOf(termList.get(row))), 0, row + 1);
        }

        // Vessels block
        for (ScheduleEntry e : plan.getSchedule()) {
            int row = termList.indexOf(e.getTerminalId()) + 1;

            OffsetDateTime st = OffsetDateTime.parse(e.getStartTime(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            OffsetDateTime en = OffsetDateTime.parse(e.getEndTime(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);

            int colStart = tLine.indexOf(st) + 1;
            int colEnd = tLine.indexOf(en) + 1;
            int span = Math.max(1, colEnd - colStart);

            StackPane block = new StackPane();
            block.setPrefSize(100, 40);
            block.setStyle("-fx-background-color: lightblue; -fx-border-color: black;");
            block.getChildren().add(new Label(e.getVesselId()));

            planGrid.add(block, colStart, row, span, 1);
        }
        List<ShipRow> ships = plan.getShips().stream()
                .map(ShipRow::new)
                .sorted(Comparator.comparing(s -> s.getDto().getArrivalTime())) // date sort
                .collect(Collectors.toList());

        shipTable.getItems().setAll(ships);

    }

    private void renderPlanToGrid(PlanResponse plan, GridPane target) {
        target.getChildren().clear();
        Set<Integer> terminals = new TreeSet<>();
        Set<OffsetDateTime> times = new TreeSet<>();

        for (ScheduleEntry e : plan.getSchedule()) {
            terminals.add(e.getTerminalId());
            times.add(OffsetDateTime.parse(e.getStartTime(), DateTimeFormatter.ISO_OFFSET_DATE_TIME));
            times.add(OffsetDateTime.parse(e.getEndTime(), DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        }

        List<Integer> termList = new ArrayList<>(terminals);
        List<OffsetDateTime> timeList = new ArrayList<>(times);

        for (int col = 0; col < timeList.size(); col++) {
            target.add(new Label(timeList.get(col).toLocalTime().toString()), col + 1, 0);
        }
        for (int row = 0; row < termList.size(); row++) {
            target.add(new Label(String.valueOf(termList.get(row))), 0, row + 1);
        }

        for (ScheduleEntry e : plan.getSchedule()) {
            int row = termList.indexOf(e.getTerminalId()) + 1;
            OffsetDateTime st = OffsetDateTime.parse(e.getStartTime(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            OffsetDateTime en = OffsetDateTime.parse(e.getEndTime(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);

            int colStart = timeList.indexOf(st) + 1;
            int colEnd = timeList.indexOf(en) + 1;
            int span = Math.max(1, colEnd - colStart);

            StackPane block = new StackPane();
            block.setPrefSize(100, 40);
            block.setStyle("-fx-background-color: lightgreen; -fx-border-color: black;");
            block.getChildren().add(new Label(e.getVesselId()));
            target.add(block, colStart, row, span, 1);
        }
    }

    private void renderMetrics(Metrics m, Label total, Label delayed, Label util) {
        total.setText("Total vessels: " + m.getTotalVessels());
        delayed.setText("Loaded: -"); // точных данных нет
        util.setText("Avg time to load: " + String.format("%.1f%%", m.getOverallUtilization() * 100));
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