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
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AppController {

    @FXML private ComboBox<String> algorithmSelector;

    @FXML private GridPane manualGrid;
    @FXML private GridPane mlGrid;

    @FXML private AnchorPane scheduleRoot;
    private ScheduleController scheduleController;

    @FXML private Label planInfoLabel;
    @FXML private Label statusLabel;
    @FXML private Label terminalCount;
    @FXML private Label vesselCount;
    @FXML private Label eventCount;

    @FXML private TableView<ShipRow> shipTable;
    @FXML private TableColumn<ShipRow, String> shipColumn;
    @FXML private TableColumn<ShipRow, String> arrivalColumn;
    @FXML private TableColumn<ShipRow, String> cargoColumn;
    @FXML private TableColumn<ShipRow, String> priorityColumn;
    @FXML private TableColumn<ShipRow, String> lengthColumn;
    @FXML private TableColumn<ShipRow, String> draftColumn;
    @FXML private TableColumn<ShipRow, String> durationColumn;

    private final BackendClient backendClient = BackendClient.get();
    private static final int SLOT_WIDTH = 60;

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

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/schedule_view.fxml"));
            AnchorPane pane = loader.load();
            scheduleController = loader.getController();
            scheduleRoot.getChildren().setAll(pane);
            AnchorPane.setTopAnchor(pane, 0.0);
            AnchorPane.setRightAnchor(pane, 0.0);
            AnchorPane.setBottomAnchor(pane, 0.0);
            AnchorPane.setLeftAnchor(pane, 0.0);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML private void onGeneratePlan() {
        setStatus("Requesting plan...");
        ConditionsDto dto = new ConditionsDto(manualTerminals, manualShips, manualEvents);
        backendClient.generatePlan(dto).ifPresentOrElse(plan -> {
            scheduleController.renderPlan(plan);
            drawPlanTimeline(plan);
            planInfoLabel.setText("ID: " + plan.getScenarioId() + " · " + plan.getAlgorithmUsed());
            setStatus("Ready");
        }, () -> {
            setStatus("Failed to get plan");
            showError("Error", "No plan received");
        });
    }

    private void refreshShipTable() {
        shipTable.getItems().setAll(
                manualShips.stream().map(ShipRow::new).toList()
        );
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
            refreshShipTable();
            setStatus("Random data loaded");
        }, () -> {
            setStatus("Failed to load data");
            showError("Error", "No data received");
        });
    }

    @FXML private void openTerminalSettings() {
        manualTerminals = openSettingsDialog("/terminal_settings.fxml", manualTerminals);
        terminalCount.setText(String.valueOf(manualTerminals.size()));
    }

    @FXML private void openVesselSettings() {
        manualShips = openSettingsDialog("/vessels_settings.fxml", manualShips);
        vesselCount.setText(String.valueOf(manualShips.size()));
        refreshShipTable();
    }

    @FXML private void openEventsSettings() {
        manualEvents = openSettingsDialog("/events_settings.fxml", manualEvents);
        eventCount.setText(String.valueOf(manualEvents.size()));
    }

    private <T> List<T> openSettingsDialog(String fxml, List<T> initial) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            SettingsResult<T> ctrl = loader.getController();
            ctrl.setData(initial);
            Stage dialog = new Stage();
            dialog.setTitle("Settings");
            dialog.setScene(new Scene(root));
            dialog.showAndWait();
            return ctrl.collectResult();
        } catch (IOException e) {
            e.printStackTrace();
            return initial;
        }
    }

    private void setStatus(String msg) {
        statusLabel.setText("[Port] " + msg);
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setTitle(title);
        alert.showAndWait();
    }
    private void drawPlanTimeline(PlanResponseDto plan) {

        /* same borders by ships + events */
        LocalDateTime min = null, max = null;

        for (ScheduleItemDto it : plan.getSchedule()) {
            LocalDateTime s = parseDateTime(it.getStartTime());
            LocalDateTime e = parseDateTime(it.getEndTime());
            if (s != null && (min == null || s.isBefore(min))) min = s;
            if (e != null && (max == null || e.isAfter(max)))  max = e;
        }
        for (EventDto ev : manualEvents) {
            if (ev.getStart() != null && (min == null || ev.getStart().isBefore(min))) min = ev.getStart();
            if (ev.getEnd()   != null && (max == null || ev.getEnd().isAfter(max)))     max = ev.getEnd();
        }
        if (min == null || max == null) return;        // empty set

        long hoursTotal = ChronoUnit.HOURS.between(min, max) + 1;

        /* grid */
        prepareGrid(manualGrid, hoursTotal);
        prepareGrid(mlGrid,     hoursTotal);

        /* vessels — only on mlGrid */
        for (ScheduleItemDto it : plan.getSchedule()) {
            int row  = findTerminalRow(it.getTerminalId());
            int c0   = col(parseDateTime(it.getStartTime()), min);
            int span = (int) ChronoUnit.HOURS.between(
                    parseDateTime(it.getStartTime()),
                    parseDateTime(it.getEndTime())) + 1;
            addBlock(mlGrid, row, c0, span,
                    "-fx-background-color:#8BC34A;-fx-text-fill:white;",
                    it.getVesselId());
        }

        /* events — on bought grids */
        for (EventDto ev : manualEvents) {
            int c0   = col(ev.getStart(), min);
            int span = (int) ChronoUnit.HOURS.between(ev.getStart(), ev.getEnd()) + 1;
            boolean closure = ev.getTerminalId() != null && !ev.getTerminalId().isBlank();

            if (closure) {
                int r = findTerminalRow(ev.getTerminalId());
                addBlock(manualGrid, r, c0, span, "-fx-background-color: rgba(255,0,0,0.4);", "");
                addBlock(mlGrid,     r, c0, span, "-fx-background-color: rgba(255,0,0,0.4);", "");
            } else {               // WEATHER → all rows
                for (int r = 0; r < manualTerminals.size(); r++) {
                    addBlock(manualGrid, r, c0, span, "-fx-background-color: rgba(30,144,255,0.3);", "");
                    addBlock(mlGrid,     r, c0, span, "-fx-background-color: rgba(30,144,255,0.3);", "");
                }
            }
        }
    }

    private void prepareGrid(GridPane grid, long hoursTotal) {
        grid.getChildren().clear();
        grid.getColumnConstraints().clear();

        /* 0th column for terminal name */
        grid.getColumnConstraints().add(new ColumnConstraints(80));
        for (int c = 0; c < hoursTotal; c++)
            grid.getColumnConstraints().add(new ColumnConstraints(SLOT_WIDTH));

        grid.getRowConstraints().clear();
        for (int r = 0; r < manualTerminals.size(); r++) {
            grid.getRowConstraints().add(new RowConstraints(30));
            // id
            String caption = Optional.ofNullable(manualTerminals.get(r).getName())
                    .filter(n -> !n.isBlank())
                    .orElse(String.valueOf(manualTerminals.get(r).getId()));
            Label l = new Label(caption);
            l.setStyle("-fx-font-size:10px;-fx-text-fill:gray;");
            GridPane.setRowIndex(l, r);
            GridPane.setColumnIndex(l, 0);
            grid.getChildren().add(l);
        }
    }

    private int findTerminalRow(String termIdStr) {
        for (int i = 0; i < manualTerminals.size(); i++)
            if (String.valueOf(manualTerminals.get(i).getId()).equals(termIdStr))
                return i;

        /* phantom terminal if an unknown id came */
        TerminalDto ph = new TerminalDto();
        ph.setId(Integer.parseInt(termIdStr));
        ph.setName("T-" + termIdStr);
        manualTerminals.add(ph);
        return manualTerminals.size() - 1;
    }

    private int col(LocalDateTime t0, LocalDateTime base) {
        return (int) ChronoUnit.HOURS.between(base, t0) + 1;   // +1 — 0th column for signatures
    }

    private void addBlock(GridPane g, int row, int c0, int span, String css, String text) {
        Label b = new Label(text);
        b.setStyle(css);
        b.setMinWidth((double) SLOT_WIDTH * span);
        b.setMaxWidth(Region.USE_PREF_SIZE);
        GridPane.setRowIndex(b, row);
        GridPane.setColumnIndex(b, c0);
        GridPane.setColumnSpan(b, span);
        g.getChildren().add(b);
    }
    private LocalDateTime parseDateTime(String s) {
        if (s == null || s.isBlank()) return null;
        try {                          // ISO-8601 “2025-06-13T10:00”
            return LocalDateTime.parse(s);
        } catch (DateTimeParseException ex) {      // fallback: “yyyy-MM-dd HH:mm”
            return LocalDateTime.parse(
                    s.replace(' ', 'T'),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
            );
        }
    }
}
