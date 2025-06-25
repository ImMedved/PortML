package com.portmanager.ui.board;

import com.portmanager.ui.ShipInfoDialog;
import com.portmanager.ui.model.*;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.time.*;
import java.util.*;

/** Editable diagram of the manual plan (drag-and-drop + runtime checks). */
public class ManualBoardController {

    /* ---------- constants ---------- */
    private static final double LABEL_W = 120;
    private static final double ROW_H   = 40;
    private static final double HOUR_W  = 10; // size horizontal of every hour/day

    /* ---------- ui & data ---------- */
    private Pane pane;
    private Label errorLabel;

    private List<TerminalDto> terminals = List.of();
    private List<EventDto> events       = List.of();
    private List<ShipDto> ships         = List.of();

    private OffsetDateTime horizonStart;
    private OffsetDateTime horizonEnd;

    /* helpers */
    private final Map<ShipDto, Rectangle> viewByShip = new HashMap<>();
    private final Map<ShipDto, Text> labelByShip     = new HashMap<>();
    private List<String> termList = List.of();

    /* ---------- public api ---------- */

    public void attach(Pane pane, Label err,
                       List<TerminalDto> terms,
                       List<EventDto> evs,
                       List<ShipDto> ships) {
        this.pane       = pane;
        this.errorLabel = err;
        this.terminals  = terms;
        this.events     = evs;
        this.ships      = ships;
    }

    /** Redrawing the entire manual plan (ML copy or after UPDATE). */
    public void renderPlan(PlanResponseDto plan) {
        pane.getChildren().clear();
        viewByShip.clear();
        labelByShip.clear();
        if (plan == null) return;

        /* -------- prepare horizon & terminals (in order of manualTerms!) -------- */
        termList = terminals.stream()
                .map(t -> String.valueOf(t.getId()))
                .toList();

        horizonStart = OffsetDateTime.MAX;
        horizonEnd   = OffsetDateTime.MIN;

        for (ScheduleItemDto si : plan.getSchedule()) {
            OffsetDateTime s = OffsetDateTime.parse(si.getStartTime());
            OffsetDateTime e = OffsetDateTime.parse(si.getEndTime());
            if (s.isBefore(horizonStart)) horizonStart = s;
            if (e.isAfter(horizonEnd))    horizonEnd   = e;
        }
        long totalHours = Math.max(1, Duration.between(horizonStart, horizonEnd).toHours());

        pane.setPrefWidth(LABEL_W + totalHours * HOUR_W);
        pane.setPrefHeight(termList.size() * ROW_H + 30);

        drawGrid(totalHours);
        drawEvents();
        drawShips(plan);
    }

    /* ---------- drawing ---------- */

    public void updateData(List<TerminalDto> terms,
                           List<EventDto> evs,
                           List<ShipDto> ships) {
        this.terminals = terms;
        this.events    = evs;
        this.ships     = ships;
    }
    private void drawGrid(long totalHours) {
        pane.getChildren().clear();

        /* vertical lines & dates */
        OffsetDateTime cur = horizonStart;
        for (long h = 0; h <= totalHours; h++) {
            if (cur.getHour() == 0) {
                double x = LABEL_W + h * HOUR_W;
                Line l = new Line(x, 0, x, pane.getPrefHeight());
                l.setStroke(cur.getDayOfWeek() == DayOfWeek.MONDAY ? Color.DARKGRAY : Color.GRAY);
                l.setStrokeWidth(cur.getDayOfWeek() == DayOfWeek.MONDAY ? 2 : 1);
                pane.getChildren().add(l);

                Text d = new Text(cur.toLocalDate().toString());
                d.setX(x + 2);
                d.setY(14);                       // separate line
                pane.getChildren().add(d);
            }
            cur = cur.plusHours(1);
        }

        /* terminal labels + horizontal lines */
        for (int i = 0; i < termList.size(); i++) {
            Text t = new Text(termList.get(i));
            t.setX(4);
            t.setY(ROW_H * (i + 1) - 8);
            pane.getChildren().add(t);

            Line h = new Line(0, ROW_H * (i + 1),
                    LABEL_W + totalHours * HOUR_W, ROW_H * (i + 1));
            h.setStroke(Color.LIGHTGRAY);
            pane.getChildren().add(h);
        }
    }

    private void drawEvents() {
        for (EventDto ev : events) {
            if (ev.getStart() == null || ev.getEnd() == null) continue;

            OffsetDateTime s = ev.getStart().atOffset(ZoneOffset.UTC);
            OffsetDateTime e = ev.getEnd().atOffset(ZoneOffset.UTC);
            double x = LABEL_W + durHours(horizonStart, s) * HOUR_W;
            double w = durHours(s, e) * HOUR_W;

            if (ev.getTerminalId() == null || ev.getTerminalId().isBlank()) {
                Rectangle r = new Rectangle(x, 0, w, termList.size() * ROW_H);
                r.setFill(Color.rgb(30,144,255,0.25));
                pane.getChildren().add(r);
            } else {
                int row = termList.indexOf(ev.getTerminalId());
                if (row < 0) continue;
                Rectangle r = new Rectangle(x, row * ROW_H, w, ROW_H);
                r.setFill(Color.rgb(255,0,0,0.35));
                pane.getChildren().add(r);
            }
        }
    }

    private void drawShips(PlanResponseDto plan) {
        for (ScheduleItemDto si : plan.getSchedule()) {
            ShipDto dto = findShip(si.getVesselId());
            if (dto == null) continue; // safety

            int row = termList.indexOf(si.getTerminalId());
            if (row < 0) continue;

            OffsetDateTime s = OffsetDateTime.parse(si.getStartTime());
            double x = LABEL_W + durHours(horizonStart, s) * HOUR_W;
            double y = row * ROW_H;

            double w = dto.getEstDurationHours() * HOUR_W;
            double h = ROW_H - 2;

            Rectangle r = new Rectangle(x, y, w, h);
            r.setFill(Color.web("#42A5F5"));
            r.setStroke(Color.BLACK);
            r.setCursor(Cursor.OPEN_HAND);
            pane.getChildren().add(r);

            Text lbl = new Text(dto.getId());
            lbl.setX(x + 4);
            lbl.setY(y + h * 0.65);
            pane.getChildren().add(lbl);

            viewByShip.put(dto, r);
            labelByShip.put(dto, lbl);

            installDragHandlers(dto, r, lbl);
        }
    }

    /* ---------- drag-and-drop ---------- */

    private void installDragHandlers(ShipDto dto, Rectangle r, Text label) {
        final Delta drag = new Delta();

        r.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            drag.startX = e.getX();
            drag.startY = e.getY();
            r.toFront(); label.toFront();
            r.setCursor(Cursor.CLOSED_HAND);
        });

        r.addEventFilter(MouseEvent.MOUSE_DRAGGED, e -> {
            double dx = e.getX() - drag.startX;
            double dy = e.getY() - drag.startY;

            r.setX(r.getX() + dx);
            r.setY(r.getY() + dy);
            label.setX(label.getX() + dx);
            label.setY(label.getY() + dy);

            drag.startX = e.getX();
            drag.startY = e.getY();
        });

        r.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> {
            r.setCursor(Cursor.OPEN_HAND);
            boolean ok = snapAndValidate(dto, r, label);
            if (!ok) showError("❌ " + errorReason);
            else     clearError();
        });

        /* click → edit (only info for now) */
        r.setOnMouseClicked(e -> { if (e.getClickCount() == 2) ShipInfoDialog.show(dto); });
        label.setOnMouseClicked(r.getOnMouseClicked());
    }

    /* auxiliary holder for drag */
    private static final class Delta { double startX, startY; }

    private String errorReason = "";

    /** Returns true if everything went well, otherwise returns the block to its place. */
    private boolean snapAndValidate(ShipDto ship, Rectangle r, Text lbl) {
        /* we calculate the expected position */
        int row = (int) ((r.getY() + 1) / ROW_H);         // rounding down
        if (row < 0 || row >= termList.size()) {
            errorReason = ship.getId() + ": out of terminal range";
            restore(ship); return false;
        }

        String termId = termList.get(row);
        OffsetDateTime newStart = horizonStart.plusHours(
                Math.round((r.getX() - LABEL_W) / HOUR_W)
        );
        if (newStart.isBefore(horizonStart)) {
            errorReason = ship.getId() + ": goes beyond the left border";
            restore(ship); return false;
        }

        /* --- checks --- */
        TerminalDto term = findTerminal(termId);
        if (newStart.isBefore(ship.getArrivalTime().atOffset(ZoneOffset.UTC))) {
            errorReason = ship.getId() + ": hasn't arrived yet";
            restore(ship); return false;
        }
        if (!term.getAllowedCargoTypes().contains(ship.getCargoType())
                || ship.getDraft() > term.getMaxDraft()
                || ship.getLength() > term.getMaxLength()) {
            errorReason = ship.getId() +
                    ": the terminal is not suitable " + termId + " (cargo/length/draft)";
            restore(ship); return false;
        }
        /* intersection with events */
        OffsetDateTime newEnd = newStart.plusHours((long) ship.getEstDurationHours());
        for (EventDto ev : events) {
            if (ev.getTerminalId() == null || !ev.getTerminalId().equals(termId)) continue;
            OffsetDateTime es = ev.getStart().atOffset(ZoneOffset.UTC);
            OffsetDateTime ee = ev.getEnd().atOffset(ZoneOffset.UTC);
            if (newStart.isBefore(ee) && es.isBefore(newEnd)) {
                errorReason = ship.getId() + ": the window is closed (" + ev.getEventType() + ")";
                restore(ship); return false;
            }
        }
        /* terminal length check */
        double busyLen = ship.getLength();    // length of the moving
        for (Map.Entry<ShipDto, Rectangle> e : viewByShip.entrySet()) {
            ShipDto other = e.getKey();
            if (other == ship) continue;
            Rectangle or = e.getValue();
            int otherRow = (int) (or.getY() / ROW_H);
            if (otherRow != row) continue;

            OffsetDateTime os = calcStartByX(or.getX());
            OffsetDateTime oe = os.plusHours((long) other.getEstDurationHours());

            if (newStart.isBefore(oe) && os.isBefore(newEnd))
                busyLen += other.getLength();
        }
        if (busyLen > term.getMaxLength()) {
            errorReason = ship.getId() + ": does not fit in length (busy)";
            restore(ship); return false;
        }

        /* --- passed ---: snap coordinates on the grid */
        double snapX = LABEL_W + durHours(horizonStart, newStart) * HOUR_W;
        double snapY = row * ROW_H;

        r.setX(snapX); r.setY(snapY);
        lbl.setX(snapX + 4); lbl.setY(snapY + (ROW_H - 2) * 0.65);

        /* you can update internal ScheduleItemDto if needed */

        return true;
    }

    private OffsetDateTime calcStartByX(double x) {
        long hrs = Math.round((x - LABEL_W) / HOUR_W);
        return horizonStart.plusHours(hrs);
    }

    private void restore(ShipDto s) {
        Rectangle r = viewByShip.get(s);
        Text lbl    = labelByShip.get(s);
        /* return to old coordinates */
        r.setX(r.getLayoutX()); r.setY(r.getLayoutY());
        lbl.setX(r.getX() + 4); lbl.setY(r.getY() + (ROW_H - 2) * 0.65);
    }

    private void showError(String msg) { errorLabel.setTextFill(Color.RED); errorLabel.setText(msg); }
    private void clearError()          { errorLabel.setText(""); }

    /* ---------- tiny helpers ---------- */
    private ShipDto findShip(String id) {
        return ships.stream().filter(s -> s.getId().equals(id)).findFirst().orElse(null);
    }
    private TerminalDto findTerminal(String id) {
        return terminals.stream().filter(t -> String.valueOf(t.getId()).equals(id)).findFirst().orElse(null);
    }
    private static long durHours(OffsetDateTime a, OffsetDateTime b) {
        return Duration.between(a, b).toHours();
    }
}
