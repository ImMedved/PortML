package com.portmanager.ui;

import com.portmanager.ui.controller.ScheduleController;
import com.portmanager.ui.controller.SettingsResult;
import com.portmanager.ui.model.*;
import com.portmanager.ui.service.BackendClient;
import javafx.application.Platform;
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
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;

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

    @FXML private ScrollPane manualScroll;
    @FXML private ScrollPane mlScroll;
    private PlanResponseDto lastPlan;
    private double slotWidth = 60;

    private final BackendClient backendClient = BackendClient.get();
    private static final int SLOT_WIDTH = 60;
    private static final double MIN_SLOT = 24;  // not less than 24 px, otherwise the blocks disappear

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

        manualScroll.viewportBoundsProperty().addListener((o,ov,nv) -> {
            if (lastPlan != null && nv.getWidth()>0) Platform.runLater(() -> drawPlanTimeline(lastPlan));
        });

        mlScroll.viewportBoundsProperty().addListener((o,ov,nv) -> {
            if (lastPlan != null && nv.getWidth()>0) Platform.runLater(() -> drawPlanTimeline(lastPlan));
        });

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
            scheduleController.fitHeightToRows();
            drawPlanTimeline(plan);
            planInfoLabel.setText("ID: " + plan.getScenarioId() + " · " + plan.getAlgorithmUsed());
            lastPlan = plan;
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

        /* 1. Collect terminals and timestamps */
        LinkedHashSet<String> termSet = new LinkedHashSet<>();

        for (TerminalDto t : manualTerminals) {           // consider all known terminals
            termSet.add(String.valueOf(t.getId()));
        }

        TreeSet<OffsetDateTime> timeSet = new TreeSet<>();

        for (ScheduleItemDto s : plan.getSchedule()) {
            termSet.add(s.getTerminalId());
            timeSet.add(parseOffset(s.getStartTime()));
            timeSet.add(parseOffset(s.getEndTime()));
        }
        for (EventDto ev : manualEvents) {
            if (ev.getTerminalId() != null && !ev.getTerminalId().isBlank())
                termSet.add(ev.getTerminalId());
            if (ev.getStart() != null) timeSet.add(ev.getStart().atOffset(ZoneOffset.UTC));
            if (ev.getEnd()   != null) timeSet.add(ev.getEnd().atOffset(ZoneOffset.UTC));
        }
        if (timeSet.size() < 2 || termSet.isEmpty()) return;

        List<String> terms = new ArrayList<>(termSet);
        List<OffsetDateTime> times = new ArrayList<>(timeSet);

        /* 2. We adjust the slot and line sizes to the available window */
        double freeW = manualScroll.getViewportBounds().getWidth()  - 80;   // 80px – left column
        if (freeW > 0) slotWidth = Math.max(MIN_SLOT, freeW / times.size());

        double freeH = manualScroll.getViewportBounds().getHeight() - 30;   // 30px – row of dates
        double rowH  = Math.max(24, freeH / terms.size());                  // every line ≥24px

        /* 3. Gird builder */
        prepareGridWithHeadings(manualGrid, terms, times, rowH);
        prepareGridWithHeadings(mlGrid,     terms, times, rowH);

        /* 4. Vessels (only in mlGrid) */
        for (ScheduleItemDto s : plan.getSchedule()) {
            int row = 1 + terms.indexOf(s.getTerminalId());
            int c0  = 1 + times.indexOf(parseOffset(s.getStartTime()));
            int cE  = 1 + times.indexOf(parseOffset(s.getEndTime()));
            int span= Math.max(1, cE - c0);

            addBlock(mlGrid, row, c0, span,
                    "-fx-background-color:#8BC34A;-fx-border-color:black;",
                    s.getVesselId(), rowH);
        }

        /* 5. 2 girds events */
        for (EventDto ev : manualEvents) {
            int c0  = 1 + times.indexOf(ev.getStart().atOffset(ZoneOffset.UTC));
            int cE  = 1 + times.indexOf(ev.getEnd().atOffset(ZoneOffset.UTC));
            int spn = Math.max(1, cE - c0);

            if (ev.getTerminalId() != null && !ev.getTerminalId().isBlank()) {
                int row = 1 + terms.indexOf(ev.getTerminalId());
                addBlock(manualGrid,row,c0,spn,"-fx-background-color:rgba(255,0,0,0.35);","",rowH);
                addBlock(mlGrid,    row,c0,spn,"-fx-background-color:rgba(255,0,0,0.35);","",rowH);
            } else {   // weather → каждый ряд
                for (int r = 1; r <= terms.size(); r++) {
                    addBlock(manualGrid,r,c0,spn,"-fx-background-color:rgba(30,144,255,0.25);","",rowH);
                    addBlock(mlGrid,    r,c0,spn,"-fx-background-color:rgba(30,144,255,0.25);","",rowH);
                }
            }
        }

        lastPlan = plan;   // resize cache
    }

    private OffsetDateTime parseOffset(String iso) {
        return OffsetDateTime.parse(iso, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    private void prepareGridWithHeadings(GridPane g,
                                         List<String> terms,
                                         List<OffsetDateTime> times,
                                         double rowH) {

        g.getChildren().clear();
        g.getColumnConstraints().clear();
        g.getRowConstraints().clear();

        /* columns: 0 – terminal signatures; then – temporary */
        g.getColumnConstraints().add(new ColumnConstraints(80));
        for (int i = 0; i < times.size(); i++)
            g.getColumnConstraints().add(new ColumnConstraints(slotWidth));

        /* row 0 – time signatures */
        for (int c = 0; c < times.size(); c++) {
            String tLabel = times.get(c).format(DateTimeFormatter.ofPattern("MM-dd HH:mm"));
            Label l = new Label(tLabel);
            l.setStyle("-fx-font-size:9px;");
            GridPane.setRowIndex(l, 0);
            GridPane.setColumnIndex(l, c + 1);
            g.getChildren().add(l);
        }

        /* terminal lines */
        for (int r = 0; r < terms.size(); r++) {
            g.getRowConstraints().add(new RowConstraints(rowH));
            Label l = new Label(terms.get(r));          // signature as is
            GridPane.setRowIndex(l, r + 1);
            GridPane.setColumnIndex(l, 0);
            g.getChildren().add(l);
        }
    }

    private void addBlock(GridPane g,
                          int row, int col, int span,
                          String css, String txt,
                          double rowH) {
        StackPane b = new StackPane(new Label(txt));
        b.setPrefWidth(slotWidth * span);
        b.setPrefHeight(rowH - 2);
        b.setStyle(css);
        GridPane.setRowIndex(b, row);
        GridPane.setColumnIndex(b, col);
        GridPane.setColumnSpan(b, span);
        g.getChildren().add(b);
    }

}
