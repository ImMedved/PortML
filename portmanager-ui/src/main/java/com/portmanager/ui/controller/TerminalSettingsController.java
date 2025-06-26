package com.portmanager.ui.controller;

import com.portmanager.ui.model.TerminalDto;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DoubleStringConverter;
import javafx.stage.Stage;

import java.util.*;

public class TerminalSettingsController implements SettingsResult<TerminalDto> {

    /* ---------- FXML ---------- */
    @FXML private TableView<TerminalDto> terminalTable;
    @FXML private TableColumn<TerminalDto,Number> idColumn;
    @FXML private TableColumn<TerminalDto,Double> lengthColumn, draftColumn;
    @FXML private TableColumn<TerminalDto,String> nameColumn, cargoColumn, fuelColumn;

    /* ---------- data ---------- */
    private final ObservableList<TerminalDto> terminals = FXCollections.observableArrayList();
    private static final List<String> CARGO_ALL = Arrays.asList("container","general","bulk","oil","lng");
    private static final List<String> FUEL_ALL  = Arrays.asList("diesel","lng");

    @FXML
    public void initialize(){

        terminalTable.setEditable(true);
        terminalTable.setItems(terminals);

        idColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getId()));
        nameColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getName()));
        lengthColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getMaxLength()));
        draftColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getMaxDraft()));
        cargoColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(String.join(", ", d.getValue().getAllowedCargoTypes())));
        fuelColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(String.join(", ", d.getValue().getFuelSupported())));

        /* editors */
        nameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nameColumn.setOnEditCommit(e -> e.getRowValue().setName(e.getNewValue()));

        lengthColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        lengthColumn.setOnEditCommit(e -> {
            e.getRowValue().setMaxLength(e.getNewValue().doubleValue());
        });

        draftColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        draftColumn.setOnEditCommit(e -> {
            e.getRowValue().setMaxDraft(e.getNewValue().doubleValue());
        });

        cargoColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        cargoColumn.setOnEditCommit(e -> e.getRowValue().setAllowedCargoTypes(parseList(e.getNewValue(), CARGO_ALL)));

        fuelColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        fuelColumn.setOnEditCommit(e -> e.getRowValue().setFuelSupported(parseList(e.getNewValue(), FUEL_ALL)));
    }

    private List<String> parseList(String text, List<String> allowed){
        List<String> res = new ArrayList<>();
        for(String tok : text.split(",")){
            String t = tok.trim().toLowerCase();
            if(allowed.contains(t)) res.add(t);
        }
        return res.isEmpty() ? List.of(allowed.get(0)) : res;
    }

    /* toolbar */
    @FXML private void onAddTerminal(){
        TerminalDto t = new TerminalDto();
        t.setId(1000 + terminals.size());
        t.setName("T" + t.getId());
        t.setMaxLength(250);
        t.setMaxDraft(10);
        t.setAllowedCargoTypes(List.of("container"));
        t.setFuelSupported(List.of("diesel"));
        terminals.add(t);
    }
    @FXML private void onConfirm(){ ((Stage)terminalTable.getScene().getWindow()).close(); }

    /* interface */
    @Override public List<TerminalDto> getData(){ return terminals; }
    @Override public void setData(List<TerminalDto> init){ terminals.setAll(init); }
    @Override public List<TerminalDto> collectResult(){ return new ArrayList<>(terminals); }
}
