package com.portmanager.ui.board;

import com.portmanager.ui.model.PlanResponseDto;
import com.portmanager.ui.model.ScheduleItemDto;
import javafx.scene.layout.Pane;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Read-only board that visualises ML schedule.
 */
public class MlBoardController extends BoardController {

    @Override
    public void renderPlan(PlanResponseDto plan) {
        if (plan == null) return;

        DateTimeFormatter ISO = DateTimeFormatter.ISO_DATE_TIME;

        for (ScheduleItemDto si : plan.getSchedule()) {
            int row = rowIndexForTerminal(si.getTerminalId());
            if (row < 0) continue;

            LocalDateTime start = LocalDateTime.parse(si.getStartTime(), ISO);
            LocalDateTime end   = LocalDateTime.parse(si.getEndTime(),   ISO);

            int colStart = columnFor(start);
            int span     = (int) java.time.Duration.between(start, end).toHours();

            Pane block = new Pane();
            block.getStyleClass().add("ml-block");
            grid.add(block, colStart + 1, row, span, 1);
        }
    }
}
