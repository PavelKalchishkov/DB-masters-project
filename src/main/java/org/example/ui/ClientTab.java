package org.example.ui;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.example.dao.ClientDao;
import org.example.model.Client;
import java.math.BigDecimal;
import java.sql.SQLException;

public final class ClientTab {

    private ClientTab() {}

    public static Tab build() {
        var dao = new ClientDao();

        TableView<Client> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Client, Long> colPersonId = new TableColumn<>("Person ID");
        colPersonId.setCellValueFactory(c ->
                new SimpleLongProperty(c.getValue().personId()).asObject()
        );

        TableColumn<Client, String> colBudget = new TableColumn<>("Budget");
        colBudget.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().budget() == null ? "" : c.getValue().budget().toPlainString())
        );

        TableColumn<Client, String> colArea = new TableColumn<>("Area");
        colArea.setCellValueFactory(c ->
                new SimpleStringProperty(nvl(c.getValue().areaInterestedIn()))
        );

        table.getColumns().addAll(colPersonId, colBudget, colArea);

        // Form
        TextField personId = new TextField();
        personId.setPromptText("existing person_id");

        TextField budget = new TextField();
        budget.setPromptText("e.g. 150000");

        TextField area = new TextField();
        area.setPromptText("e.g. Sofia - Lozenets");

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.addRow(0, new Label("Person ID"), personId);
        form.addRow(1, new Label("Budget"), budget);
        form.addRow(2, new Label("Area"), area);

        Button btnRefresh = new Button("Refresh");
        Button btnClear   = new Button("Clear");
        Button btnCreate  = new Button("Create");
        Button btnUpdate  = new Button("Update");
        Button btnDelete  = new Button("Delete");

        HBox buttons = new HBox(10, btnRefresh, btnClear, btnCreate, btnUpdate, btnDelete);

        VBox right = new VBox(12, form, buttons);
        right.setPadding(new Insets(10));
        right.setPrefWidth(380);

        BorderPane root = new BorderPane(table, null, right, null, null);
        root.setPadding(new Insets(10));

        Runnable refresh = () -> {
            try {
                table.setItems(FXCollections.observableArrayList(dao.findAll()));
            } catch (SQLException ex) {
                showError("DB error while loading clients", ex);
            }
        };

        Runnable clearForm = () -> {
            personId.clear();
            budget.clear();
            area.clear();
            table.getSelectionModel().clearSelection();
        };

        btnRefresh.setOnAction(e -> refresh.run());
        btnClear.setOnAction(e -> clearForm.run());

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldV, c) -> {
            if (c == null) return;
            personId.setText(String.valueOf(c.personId()));
            budget.setText(c.budget() == null ? "" : c.budget().toPlainString());
            area.setText(nvl(c.areaInterestedIn()));
        });

        btnCreate.setOnAction(e -> {
            try {
                Long pid = parseLong(personId.getText(), "Person ID");
                if (pid == null) return;

                BigDecimal b = parseBigDecimalOrNull(budget.getText(), "Budget");
                Client toInsert = new Client(pid, b, area.getText().trim());

                dao.insert(toInsert);
                refresh.run();
                clearForm.run();
            } catch (SQLException ex) {
                showError("DB error while creating client", ex);
            }
        });

        btnUpdate.setOnAction(e -> {
            Client selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showInfo("Select a client first.");
                return;
            }
            try {
                Long pid = parseLong(personId.getText(), "Person ID");
                if (pid == null) return;

                BigDecimal b = parseBigDecimalOrNull(budget.getText(), "Budget");
                Client updated = new Client(pid, b, area.getText().trim());

                dao.update(updated);
                refresh.run();
            } catch (SQLException ex) {
                showError("DB error while updating client", ex);
            }
        });

        btnDelete.setOnAction(e -> {
            Client selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showInfo("Select a client first.");
                return;
            }
            if (!confirm("Delete client (person_id=" + selected.personId() + ")?")) return;

            try {
                dao.delete(selected.personId());
                refresh.run();
                clearForm.run();
            } catch (SQLException ex) {
                showError("DB error while deleting client", ex);
            }
        });

        refresh.run();

        Tab tab = new Tab("Clients", root);
        tab.setClosable(false);
        return tab;
    }

    // ---- helpers ----
    private static String nvl(String s) { return s == null ? "" : s; }

    private static Long parseLong(String s, String field) {
        try {
            String t = s.trim();
            if (t.isEmpty()) {
                showInfo(field + " is required.");
                return null;
            }
            return Long.parseLong(t);
        } catch (NumberFormatException ex) {
            showInfo(field + " must be a whole number.");
            return null;
        }
    }

    private static BigDecimal parseBigDecimalOrNull(String s, String field) {
        String t = s.trim();
        if (t.isEmpty()) return null;
        try {
            return new BigDecimal(t);
        } catch (NumberFormatException ex) {
            showInfo(field + " must be a number (or blank).");
            return null;
        }
    }

    private static void showError(String title, Exception ex) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error");
        a.setHeaderText(title);
        a.setContentText(ex.getMessage());
        a.showAndWait();
    }

    private static void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Info");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private static boolean confirm(String msg) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Confirm");
        a.setHeaderText(null);
        a.setContentText(msg);
        return a.showAndWait().filter(b -> b == ButtonType.OK).isPresent();
    }
}
