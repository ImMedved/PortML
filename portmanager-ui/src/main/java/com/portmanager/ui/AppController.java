/*
 * PortManager — UI
 * Full version of AppController.java (the schedule diagram is rewritten from scratch).
 *
 * Main: controller class + nested static class TimelineDiagram,
 * which draws a Gantt diagram with cutoffs by days/weeks,
 * events and ships, supports click → ShipInfoDialog.
 */

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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.*;
import java.util.*;

/**
 * The main controller of the application.
 */
public class AppController {

    /* UI from FXML */

    @FXML private ComboBox<String> algorithmSelector;

    /** GridPanes from the old implementation (no longer used, but left in order not to touch the FXML). */
    @FXML private GridPane manualGrid;
    @FXML private GridPane mlGrid;

    @FXML private ScrollPane manualScroll;
    @FXML private ScrollPane mlScroll;

    @FXML private AnchorPane scheduleRoot;
    private ScheduleController scheduleController;

    @FXML private Label planInfoLabel, statusLabel;
    @FXML private Label terminalCount, vesselCount, eventCount;

    @FXML private TableView<ShipRow> shipTable;
    @FXML private TableColumn<ShipRow,String> shipColumn, arrivalColumn, cargoColumn,
            priorityColumn, lengthColumn, draftColumn, durationColumn;

    private final BackendClient backendClient = BackendClient.get();

    private PlanResponseDto lastPlan;

    private List<ShipDto>      manualShips  = new ArrayList<>();
    private List<TerminalDto>  manualTerms  = new ArrayList<>();
    private List<EventDto>     manualEvents = new ArrayList<>();

    private TimelineDiagram mlDiagram;

    @FXML
    public void initialize() {
        algorithmSelector.getItems().addAll("baseline", "boosting", "RL");
        algorithmSelector.setValue("baseline");

        /* table of vessels */
        shipColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getVesselId()));
        arrivalColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getArrivalTime()));
        cargoColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCargoType()));
        priorityColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPriority()));
        lengthColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getLength()));
        draftColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDraft()));
        durationColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDuration()));

        shipTable.setOnMouseClicked(e ->
                Optional.ofNullable(shipTable.getSelectionModel().getSelectedItem())
                        .ifPresent(s -> ShipInfoDialog.show(s.getDto())));

        /* timetable on bottom */
        try {
            FXMLLoader l = new FXMLLoader(getClass().getResource("/schedule_view.fxml"));
            AnchorPane pane = l.load();
            scheduleController = l.getController();
            scheduleRoot.getChildren().setAll(pane);
            AnchorPane.setTopAnchor(pane, 0.0);
            AnchorPane.setRightAnchor(pane, 0.0);
            AnchorPane.setBottomAnchor(pane, 0.0);
            AnchorPane.setLeftAnchor(pane, 0.0);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        /* new chart instead of mlGrid */
        mlDiagram = new TimelineDiagram();
        mlScroll.setFitToHeight(true);
        mlScroll.setFitToWidth(false); // disable stretching
        mlScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS); // show scroll bar
        mlScroll.setContent(mlDiagram);
    }

    /* actions */

    /** Generate Plan */
    @FXML
    private void onGeneratePlan() {
        setStatus("Requesting plan…");
        ConditionsDto dto = new ConditionsDto(manualTerms, manualShips, manualEvents);

        backendClient.generatePlan(dto).ifPresentOrElse(plan -> {
            /* table → lower right corner */
            scheduleController.renderPlan(plan);
            scheduleController.fitHeightToRows();

            /* chart */
            mlDiagram.render(plan, manualTerms, manualEvents, manualShips);

            planInfoLabel.setText("ID: " + plan.getScenarioId() +
                    " · " + plan.getAlgorithmUsed());
            lastPlan = plan;
            setStatus("Ready");
        }, () -> {
            setStatus("Failed");
            showErr("Plan was not received");
        });
    }

    /* Random Data */
    @FXML
    private void onRandomData() {
        setStatus("Requesting random data…");
        backendClient.requestRandomData(20).ifPresentOrElse(dto -> {
            manualTerms  = dto.terminals();
            manualShips  = dto.ships();
            manualEvents = dto.events();
            terminalCount.setText(String.valueOf(manualTerms.size()));
            vesselCount.setText(String.valueOf(manualShips.size()));
            eventCount.setText(String.valueOf(manualEvents.size()));
            refreshShipTable();
            setStatus("Random data loaded");
        }, () -> {
            setStatus("Failed to load data");
            showErr("No data received");
        });
    }

    /* settings dialogs */

    @FXML private void openTerminalSettings() {
        manualTerms = openSettingsDialog("/terminal_settings.fxml", manualTerms);
        terminalCount.setText(String.valueOf(manualTerms.size()));
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
            FXMLLoader l = new FXMLLoader(getClass().getResource(fxml));
            Parent root = l.load();
            SettingsResult<T> ctrl = l.getController();
            ctrl.setData(initial);
            Stage dlg = new Stage();
            dlg.setTitle("Settings");
            dlg.setScene(new Scene(root));
            dlg.showAndWait();
            return ctrl.collectResult();
        } catch (IOException e) {
            e.printStackTrace();
            return initial;
        }
    }

    /* helpers */

    private void refreshShipTable() {
        shipTable.getItems().setAll(
                manualShips.stream().map(ShipRow::new).toList()
        );
    }

    private void setStatus(String msg) { statusLabel.setText("[Port] " + msg); }

    private void showErr(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setTitle("Error");
        a.showAndWait();
    }

    /* =====================================================================
       TimelineDiagram - Absolutely positioned Gantt chart
       ===================================================================== */
    public static class TimelineDiagram extends Pane {

        /* размеры и цвета */
        private static final double LABEL_W = 120;   // on the left are the names of the terminals
        private static final double ROW_H   = 40;    // baseline height
        private static final double HOUR_W  = 10;   // width of one hour

        private OffsetDateTime horizonStart;
        private OffsetDateTime horizonEnd;

        /* internal record for storing vessel intervals */
        private record Interval(OffsetDateTime s, OffsetDateTime e) {}

        /** Complete redraw method */
        public void render(PlanResponseDto plan,
                           List<TerminalDto> terminals,
                           List<EventDto> events,
                           List<ShipDto> ships) {

            getChildren().clear();
            if (plan == null) return;

            /* data preparation */

            /* terminals (save the order of addition) */
            LinkedHashSet<String> termSet = new LinkedHashSet<>();
            terminals.forEach(t -> termSet.add(String.valueOf(t.getId())));
            plan.getSchedule().forEach(si -> termSet.add(si.getTerminalId()));
            List<String> termList = new ArrayList<>(termSet);

            /* time horizon */
            horizonStart = OffsetDateTime.MAX;
            horizonEnd   = OffsetDateTime.MIN;

            plan.getSchedule().forEach(si -> {
                OffsetDateTime s = OffsetDateTime.parse(si.getStartTime());
                OffsetDateTime e = OffsetDateTime.parse(si.getEndTime());
                if (s.isBefore(horizonStart)) horizonStart = s;
                if (e.isAfter(horizonEnd))    horizonEnd   = e;
            });
            events.forEach(ev -> {
                if (ev.getStart() != null) {
                    OffsetDateTime s = ev.getStart().atOffset(ZoneOffset.UTC);
                    if (s.isBefore(horizonStart)) horizonStart = s;
                }
                if (ev.getEnd() != null) {
                    OffsetDateTime e = ev.getEnd().atOffset(ZoneOffset.UTC);
                    if (e.isAfter(horizonEnd)) horizonEnd = e;
                }
            });

            long totalHours = Math.max(1, Duration.between(horizonStart, horizonEnd).toHours());

            /* canvas size */
            setPrefWidth(LABEL_W + totalHours * HOUR_W);
            setPrefHeight(termList.size() * ROW_H + 30);

            /* vertical grid (days/weeks) */
            drawTimeGrid(totalHours);

            /* terminal labels + horizontal lines */
            for (int idx = 0; idx < termList.size(); idx++) {
                /* подпись */
                Text t = new Text(termList.get(idx));
                t.setX(4);
                t.setY(idx * ROW_H + ROW_H * 0.7);
                getChildren().add(t);

                /* thin line */
                Line h = new Line(0,
                        idx * ROW_H,
                        LABEL_W + totalHours * HOUR_W,
                        idx * ROW_H);
                h.setStroke(Color.LIGHTGRAY);
                getChildren().add(h);
            }
            /* lower limit */
            Line bottom = new Line(0,
                    termList.size() * ROW_H,
                    LABEL_W + totalHours * HOUR_W,
                    termList.size() * ROW_H);
            bottom.setStroke(Color.LIGHTGRAY);
            getChildren().add(bottom);

            /* events */
            for (EventDto ev : events) {
                if (ev.getStart() == null || ev.getEnd() == null) continue;

                OffsetDateTime s = ev.getStart().atOffset(ZoneOffset.UTC);
                OffsetDateTime e = ev.getEnd().atOffset(ZoneOffset.UTC);

                double x = LABEL_W + durHours(horizonStart, s) * HOUR_W;
                double w = durHours(s, e) * HOUR_W;

                if (ev.getTerminalId() == null || ev.getTerminalId().isBlank()) {
                    /* weather → all vertical */
                    Rectangle r = new Rectangle(x, 0, w, termList.size() * ROW_H);
                    r.setFill(Color.rgb(30, 144, 255, 0.25));   // blue
                    getChildren().add(r);
                } else {
                    int row = termList.indexOf(ev.getTerminalId());
                    if (row < 0) continue;

                    Rectangle r = new Rectangle(x, row * ROW_H, w, ROW_H);
                    r.setFill(Color.rgb(255, 0, 0, 0.35));      // red
                    getChildren().add(r);
                }
            }

            /* calculation of "paths" for simultaneous vessels */
            Map<String, List<List<Interval>>> lanes = new HashMap<>();
            Map<ScheduleItemDto, Integer> whichLane = new HashMap<>();

            termList.forEach(t -> lanes.put(t, new ArrayList<>()));

            for (ScheduleItemDto si : plan.getSchedule()) {
                String term = si.getTerminalId();
                Interval cur = new Interval(
                        OffsetDateTime.parse(si.getStartTime()),
                        OffsetDateTime.parse(si.getEndTime())
                );

                List<List<Interval>> termLanes = lanes.get(term);
                int idx = 0;
                for (; idx < termLanes.size(); idx++)
                    if (!overlaps(cur, termLanes.get(idx))) break;

                if (idx == termLanes.size()) termLanes.add(new ArrayList<>());
                termLanes.get(idx).add(cur);
                whichLane.put(si, idx);
            }

            /* terminal line division degree */
            Map<String, Integer> laneCount = new HashMap<>();
            lanes.forEach((k, v) -> laneCount.put(k, Math.max(1, v.size())));

            /* quick access to ShipDto for a click */
            Map<String, ShipDto> shipById = new HashMap<>();
            ships.forEach(s -> shipById.put(s.getId(), s));

            /* vessels */
            for (ScheduleItemDto si : plan.getSchedule()) {
                String term = si.getTerminalId();
                int row = termList.indexOf(term);
                if (row < 0) continue;

                int lanesN = laneCount.get(term);
                double laneH = ROW_H / lanesN;
                int laneIdx = whichLane.get(si);

                OffsetDateTime s = OffsetDateTime.parse(si.getStartTime());
                OffsetDateTime e = OffsetDateTime.parse(si.getEndTime());

                double x = LABEL_W + durHours(horizonStart, s) * HOUR_W;
                double w = durHours(s, e) * HOUR_W;
                double y = row * ROW_H + laneIdx * laneH;

                Rectangle rect = new Rectangle(x, y, w, laneH - 2);
                rect.setFill(Color.web("#8BC34A"));    // light-green
                rect.setStroke(Color.BLACK);
                getChildren().add(rect);

                Text label = new Text(si.getVesselId());
                label.setX(x + 4);
                label.setY(y + laneH * 0.7);
                getChildren().add(label);

                /* click → info about the vessel */
                ShipDto dto = shipById.get(si.getVesselId());
                if (dto != null) {
                    rect.addEventHandler(MouseEvent.MOUSE_CLICKED, e2 -> ShipInfoDialog.show(dto));
                    label.addEventHandler(MouseEvent.MOUSE_CLICKED, e2 -> ShipInfoDialog.show(dto));
                }
            }
        }

        /* private helpers */

        /** Vertical lines by day, thickened - by Mondays ☕ */
        private void drawTimeGrid(long totalHours) {
            OffsetDateTime cursor = horizonStart;

            for (long h = 0; h <= totalHours; h++) {
                if (cursor.getHour() == 0) {                       // начало суток
                    double x = LABEL_W + h * HOUR_W;

                    Line l = new Line(x, 0, x, getPrefHeight());
                    if (cursor.getDayOfWeek() == DayOfWeek.MONDAY) {
                        l.setStrokeWidth(2);
                        l.setStroke(Color.DARKGRAY);
                    } else {
                        l.setStrokeWidth(1);
                        l.setStroke(Color.GRAY);
                    }
                    getChildren().add(l);

                    /* date */
                    Text d = new Text(cursor.toLocalDate().toString());
                    d.setX(x + 2); d.setY(12);
                    getChildren().add(d);
                }
                cursor = cursor.plusHours(1);
            }
        }

        private static long durHours(OffsetDateTime a, OffsetDateTime b) {
            return Duration.between(a, b).toHours();
        }

        private static boolean overlaps(Interval cur, List<Interval> lane) {
            for (Interval p : lane)
                if (cur.s.isBefore(p.e) && p.s.isBefore(cur.e)) return true;
            return false;
        }
    }
}
