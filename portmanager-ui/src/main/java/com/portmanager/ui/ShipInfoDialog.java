package com.portmanager.ui;

import com.portmanager.ui.model.ShipDto;
import javafx.scene.control.Alert;

public class ShipInfoDialog {
    public static void show(ShipDto ship) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Vessel info");
        alert.setHeaderText("Vessel: " + ship.getId());

        String content = String.format(
                "Arrival date: %s\nLength: %.1f м\nDraft: %.1f м\nCargo type: %s\nUnload time: %.1f ч",
                ship.getArrivalTime(),
                ship.getLength(),
                ship.getDraft(),
                ship.getCargoType(),
                ship.getEstDurationHours()
        );

        alert.setContentText(content);
        alert.showAndWait();
    }
}

