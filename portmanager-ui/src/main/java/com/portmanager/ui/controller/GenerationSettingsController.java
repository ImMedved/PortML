package com.portmanager.ui.controller;

import com.portmanager.ui.model.GenerationConfigDto;
import javafx.fxml.FXML;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.stage.Stage;
import lombok.Getter;

import java.util.Map;

/** Controller for generation_settings.fxml. */
public class GenerationSettingsController {

    /* spinners for ships */
    @FXML private Spinner<Integer> shipCount;
    @FXML private Spinner<Integer> pilotPercent, customsPercent, priorityPercent, tempPercent;
    @FXML private Spinner<Integer> cargoCont, cargoBulk, cargoOil, cargoLng, cargoGen;

    /* spinners for terminals */
    @FXML private Spinner<Integer> termCount;
    @FXML private Spinner<Integer> termUniv, termCont, termBulk, termOil, termLng;

    @Getter
    private GenerationConfigDto result;          // null if cancelled

    @FXML
    private void initialize() {
        // default ranges
        for (Spinner<?> s : new Spinner[]{ shipCount, termCount }) {
            ((Spinner<Integer>)s).setValueFactory(
                    new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 5000, 0));
        }
        for (Spinner<?> s : new Spinner[]{
                pilotPercent, customsPercent, priorityPercent, tempPercent,
                cargoCont, cargoBulk, cargoOil, cargoLng, cargoGen,
                termUniv, termCont, termBulk, termOil, termLng }) {

            ((Spinner<Integer>)s).setValueFactory(
                    new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 0));
        }
    }

    @FXML
    private void onCancel() { ((Stage) shipCount.getScene().getWindow()).close(); }

    @FXML
    private void onOk() {
        GenerationConfigDto cfg = new GenerationConfigDto();

        if (shipCount.getValue() > 0) cfg.setShipCount(shipCount.getValue());
        if (termCount.getValue() > 0) cfg.setTerminalCount(termCount.getValue());

        putPercent(cfg::setPilotPercent,       pilotPercent);
        putPercent(cfg::setCustomsPercent,     customsPercent);
        putPercent(cfg::setPriorityPercent,    priorityPercent);
        putPercent(cfg::setTemperaturePercent, tempPercent);

        cfg.setCargoDistribution( Map.of(
                "container", pct(cargoCont), "bulk", pct(cargoBulk),
                "oil", pct(cargoOil), "lng", pct(cargoLng),
                "general", pct(cargoGen)
        ));
        cfg.setTerminalCargoDistribution( Map.of(
                "universal", pct(termUniv), "container", pct(termCont),
                "bulk", pct(termBulk), "oil", pct(termOil),
                "lng", pct(termLng)
        ));

        this.result = cfg;
        onCancel();
    }

    /* helpers */
    private static double pct(Spinner<Integer> sp) { return sp.getValue(); }

    private static void putPercent(java.util.function.Consumer<Double> setter,
                                   Spinner<Integer> sp) {
        if (sp.getValue() > 0) setter.accept((double) sp.getValue());
    }
}
