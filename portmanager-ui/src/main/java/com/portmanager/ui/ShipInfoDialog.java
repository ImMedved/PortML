package com.portmanager.ui;

import com.portmanager.ui.model.ShipRow;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class ShipInfoDialog {

    public static void show(ShipRow row) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Vessel Information");
        alert.setHeaderText("Vessel: " + row.getVesselId());
        alert.setContentText("Arrival: " + row.getArrivalTime());
        alert.showAndWait();
    }
}
