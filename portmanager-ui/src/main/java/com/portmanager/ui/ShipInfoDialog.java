package com.portmanager.ui;

import com.portmanager.ui.model.ShipDto;
import javafx.scene.control.Alert;

/**
 * Simple modal dialog that shows **extended** vessel info.
 */
public class ShipInfoDialog {

    public static void show(ShipDto ship) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Vessel info");
        alert.setHeaderText("Vessel: " + ship.getId());

        String content = String.format("""
                Arrival date      : %s
                Length / Draft    : %.1f m / %.1f m
                Cargo / Type      : %s / %s
                Unload time (hrs) : %.1f
                Deadweight (t)    : %.0f
                Flag / IMO        : %s / %s
                Fuel / Emission   : %s / %s
                Customs clearance : %s
                Hazard class      : %s
                Temp-controlled   : %s
                Requires pilot    : %s
                Arrival window    : %s – %s
                Expected delay    : %.1f h
                From → To         : %s → %s
                """,
                ship.getArrivalTime(),
                ship.getLength(),
                ship.getDraft(),
                ship.getCargoType(),
                ship.getShipType(),
                ship.getEstDurationHours(),
                ship.getDeadweight(),
                ship.getFlagCountry(),
                ship.getImoNumber(),
                ship.getFuelType(),
                ship.getEmissionRating(),
                ship.isRequiresCustomsClearance() ? "yes" : "no",
                ship.getHazardClass(),
                ship.isTemperatureControlled() ? "yes" : "no",
                ship.isRequiresPilot() ? "yes" : "no",
                ship.getArrivalWindowStart(),
                ship.getArrivalWindowEnd(),
                ship.getExpectedDelayHours(),
                ship.getArrivalPort(),
                ship.getNextPort()
        );

        alert.setContentText(content);
        alert.showAndWait();
    }
}
