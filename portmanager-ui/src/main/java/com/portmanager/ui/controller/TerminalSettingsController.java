package com.portmanager.ui.controller;

import com.portmanager.ui.model.TerminalDto;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;

import java.util.*;

public class TerminalSettingsController {

    @FXML private TableView<TerminalDto> terminalTable;
    @FXML private TableColumn<TerminalDto, Integer> idColumn;
    @FXML private TableColumn<TerminalDto, String> nameColumn;
    @FXML private TableColumn<TerminalDto, Double> lengthColumn;
    @FXML private TableColumn<TerminalDto, Double> draftColumn;
    @FXML private TableColumn<TerminalDto, String> cargoColumn;

    private ObservableList<TerminalDto> terminals = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        terminalTable.setItems(terminals);

        idColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getId()));
        nameColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getName()));
        lengthColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getMaxLength()));
        draftColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getMaxDraft()));
        cargoColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(String.join(",", data.getValue().getAllowedCargoTypes())));

        terminalTable.setEditable(true);
        nameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
    }

    @FXML
    private void onAddTerminal() {
        TerminalDto newTerminal = new TerminalDto(
                generateRandomId(),
                "New Terminal",
                200.0,
                10.0,
                List.of("containers")
        );
        terminals.add(newTerminal);
    }

    @FXML
    private void onConfirm() {
        if (terminals.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "At least one terminal must be defined.");
            alert.showAndWait();
            return;
        }

        // TODO: сохранить terminals куда-то или передать в родительский контроллер
        ((Stage) terminalTable.getScene().getWindow()).close();
    }

    private int generateRandomId() {
        Random r = new Random();
        return 1000 + r.nextInt(9000);
    }

    public List<TerminalDto> getTerminals() {
        return terminals;
    }

    public void setTerminals(List<TerminalDto> initialData) {
        terminals.setAll(initialData);
    }
}
