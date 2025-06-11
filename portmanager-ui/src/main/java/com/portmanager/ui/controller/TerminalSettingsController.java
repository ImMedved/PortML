package com.portmanager.ui.controller;

import com.portmanager.ui.model.TerminalDto;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class TerminalSettingsController implements SettingsResult<TerminalDto> {

    @FXML private TableView<TerminalDto> terminalTable;
    @FXML private TableColumn<TerminalDto, Integer> idColumn;
    @FXML private TableColumn<TerminalDto, String> nameColumn;
    @FXML private TableColumn<TerminalDto, Double> lengthColumn;
    @FXML private TableColumn<TerminalDto, Double> draftColumn;
    @FXML private TableColumn<TerminalDto, String> cargoColumn;

    private final ObservableList<TerminalDto> terminals = FXCollections.observableArrayList();

    private static final List<String> ALL_CARGO_TYPES = Arrays.asList("containers", "vehicles", "gas", "liquid", "bulk");

    @FXML
    public void initialize() {
        terminalTable.setItems(terminals);

        idColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getId()));
        nameColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getName()));
        lengthColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getMaxLength()));
        draftColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getMaxDraft()));
        cargoColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(String.join(", ", data.getValue().getAllowedCargoTypes())));

        terminalTable.setEditable(true);

        nameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nameColumn.setOnEditCommit(event -> event.getRowValue().setName(event.getNewValue()));

        lengthColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        lengthColumn.setOnEditCommit(event -> event.getRowValue().setMaxLength(event.getNewValue()));

        draftColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        draftColumn.setOnEditCommit(event -> event.getRowValue().setMaxDraft(event.getNewValue()));

        cargoColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        cargoColumn.setOnEditCommit(event -> {
            List<String> values = new ArrayList<>();
            for (String token : event.getNewValue().split(",")) {
                String trimmed = token.trim().toLowerCase();
                if (ALL_CARGO_TYPES.contains(trimmed)) {
                    values.add(trimmed);
                }
            }
            event.getRowValue().setAllowedCargoTypes(values);
        });
    }

    @FXML
    private void onAddTerminal() {
        TerminalDto newTerminal = new TerminalDto(
                generateRandomId(), "New Terminal", 200.0, 10.0, List.of("containers")
        );
        terminals.add(newTerminal);
    }

    @FXML
    private void onConfirm() {
        if (terminals.isEmpty()) {
            showError("At least one terminal must be defined.");
            return;
        }
        closeWindow();
    }

    private int generateRandomId() {
        return 1000 + new Random().nextInt(9000);
    }

    private void closeWindow() {
        ((Stage) terminalTable.getScene().getWindow()).close();
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg);
        alert.showAndWait();
    }

    @Override
    public List<TerminalDto> getData() {
        return terminals;
    }

    @Override
    public void setData(List<TerminalDto> initialData) {
        terminals.setAll(initialData);
    }

    @Override
    public List<TerminalDto> collectResult() {
        return new ArrayList<>(terminals);
    }
}
