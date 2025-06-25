package com.portmanager.ui.board;

import com.portmanager.ui.model.*;
import javafx.scene.Node;
import javafx.scene.input.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

import java.util.Map;

/**
 * Editable board with drag-and-drop from the vessel table.
 */
public class ManualBoardController extends BoardController {

    /* ============== public API ============== */

    @Override
    public void renderPlan(PlanResponseDto plan) {
        // manual board ignores ML plan
    }

    /** Create a blue block on grid according to drop. */
    public void addShip(ShipDto ship, int row, int colStart) {
        int span = (int) ship.getEstDurationHours();
        ShipBlock block = new ShipBlock(ship);
        block.getStyleClass().add("ship-block");

        grid.add(block, colStart + 1, row, span, 1);  // +1: first column = labels
        initBlockDrag(block);
    }

    /* ============== DnD ============== */

    public void installDropTargets() {
        for (Node n : grid.getChildren())
            if (n instanceof Pane pane && GridPane.getRowIndex(pane) != null) {
                pane.setOnDragOver(this::onDragOver);
                pane.setOnDragDropped(this::onDrop);
            }
    }

    private void onDragOver(DragEvent e) {
        if (e.getDragboard().hasString()) e.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        e.consume();
    }

    private void onDrop(DragEvent e) {
        String shipId = e.getDragboard().getString();
        ShipDto ship  = findShipById(shipId);
        if (ship == null) return;

        int row = GridPane.getRowIndex((Node) e.getGestureTarget());
        int col = GridPane.getColumnIndex((Node) e.getGestureTarget()) - 1;

        if (isBlocked(row, col, ship)) return;  // intersect event -> reject

        addShip(ship, row, col);
        e.setDropCompleted(true);
    }

    private ShipDto findShipById(String id) {
        // caller passes correct list
        return null;
    }

    /* ── make ship itself draggable ── */
    private void initBlockDrag(ShipBlock block) {
        block.setOnDragDetected(ev -> {
            Dragboard db = block.startDragAndDrop(TransferMode.MOVE);
            db.setContent(Map.of(DataFormat.PLAIN_TEXT, block.ship.getId()));
            ev.consume();
        });
    }

    /* ============== checks ============== */

    private boolean isBlocked(int row, int colStart, ShipDto ship) {
        for (EventDto ev : events) {
            int evRow = ev.getType() == EventDto.EventType.WEATHER
                    ? row
                    : rowIndexForTerminal(ev.getTerminalId());
            if (evRow != row) continue;

            int evStart = columnFor(ev.getStart());
            int evEnd   = columnFor(ev.getEnd());

            int shipEnd   = colStart + (int) ship.getEstDurationHours();

            boolean overlap = colStart < evEnd && evStart < shipEnd;
            if (overlap) return true;
        }
        return false;
    }
}
