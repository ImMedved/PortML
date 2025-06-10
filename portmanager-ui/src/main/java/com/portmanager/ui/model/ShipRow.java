package com.portmanager.ui.model;

import javafx.beans.property.SimpleStringProperty;

public class ShipRow {
    private final SimpleStringProperty vesselId;
    private final SimpleStringProperty arrivalTime;

    public ShipRow(String id, String arrival) {
        this.vesselId = new SimpleStringProperty(id);
        this.arrivalTime = new SimpleStringProperty(arrival);
    }

    public String getVesselId() {
        return vesselId.get();
    }

    public String getArrivalTime() {
        return arrivalTime.get();
    }
}
