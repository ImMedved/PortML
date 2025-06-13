package com.portmanager.ui.board;

import com.portmanager.ui.model.*;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Common part for manual and ML grids.
 * Converts domain data to rectangles on GridPane.
 */
public abstract class BoardController {

    protected GridPane grid;
    protected List<TerminalDto> terminals = List.of();
    protected List<EventDto>    events    = List.of();

    private LocalDateTime horizonStart = LocalDateTime.now();

    /** Attach existing GridPane from FXML. */
    public void attach(GridPane g) {
        this.grid = g;
        grid.setAlignment(Pos.TOP_LEFT);
    }

    /** Build rows and mark closures; no ships yet. */
    public void renderConditions(ConditionsDto dto) {
        this.terminals = dto.terminals();
        this.events    = dto.events();
        determineHorizonStart(dto);
        drawRows();
        paintEvents();
    }

    public abstract void renderPlan(PlanResponseDto plan);

    /* ---------- helpers ---------- */

    protected int columnFor(LocalDateTime t) {
        long minutes = Duration.between(horizonStart, t).toMinutes();
        return (int) (minutes / 60);                     // 1 column = 1 hour
    }

    private void determineHorizonStart(ConditionsDto dto) {
        horizonStart = dto.ships().stream()
                .map(ShipDto::getArrival)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());
    }

    private void drawRows() {
        grid.getChildren().clear();
        grid.getRowConstraints().clear();

        for (int r = 0; r < terminals.size(); r++) {
            RowConstraints rc = new RowConstraints(28);   // px
            grid.getRowConstraints().add(rc);

            Label lbl = new Label(terminals.get(r).getName());
            GridPane.setHalignment(lbl, HPos.RIGHT);
            grid.add(lbl, 0, r);
        }
    }

    private void paintEvents() {
        for (EventDto ev : events) {
            int row = ev.getType() == EventDto.EventType.WEATHER ? 0 : rowIndexForTerminal(ev.getTerminalId());

            if (row < 0) continue;

            int colStart = columnFor(ev.getStart());
            int span     = (int) (Duration.between(ev.getStart(), ev.getEnd()).toHours());

            Pane block = new Pane();
            block.getStyleClass().add("event-block");
            grid.add(block, colStart + 1, row, span, 1);
        }
    }

    protected int rowIndexForTerminal(String tId) {
        for (int i = 0; i < terminals.size(); i++)
            if (String.valueOf(terminals.get(i).getId()).equals(tId)) return i;
        return -1;
    }

    /** Convenience holder for on-board rectangles. */
    protected static final class ShipBlock extends Pane {
        public final ShipDto ship;
        ShipBlock(ShipDto s) { this.ship = s; }
    }
}
