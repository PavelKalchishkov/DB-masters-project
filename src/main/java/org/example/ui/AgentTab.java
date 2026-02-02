package org.example.ui;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.example.dao.AgentDao;
import org.example.model.Agent;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;

public final class AgentTab {

    private AgentTab() {}

    public static Tab build() {
        var dao = new AgentDao();

        TableView<Agent> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Agent, Long> colPersonId = new TableColumn<>("Person ID");
        colPersonId.setCellValueFactory(c ->
                new SimpleLongProperty(c.getValue().personId()).asObject()
        );

        TableColumn<Agent, String> colSalary = new TableColumn<>("Salary");
        colSalary.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().salary() == null ? "" : c.getValue().salary().toPlainString())
        );

        TableColumn<Agent, String> colHire = new TableColumn<>("Hire date");
        colHire.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().hireDate() == null ? "" : c.getValue().hireDate().toString())
        );

        table.getColumns().addAll(colPersonId, colSalary, colHire);

        // Form
        TextField personId = new TextField();
        personId.setPromptText("existing person_id");

        TextField salary = new TextField();
        salary.setPromptText("e.g. 3500");

        DatePicker hireDate = new DatePicker();
        hireDate.setPromptText("YYYY-MM-DD");

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.addRow(0, new Label("Person ID"), personId);
        form.addRow(1, new Label("Salary"), salary);
        form.addRow(2, new Label("Hire date"), hireDate);

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
                showError("DB error while loading agents", ex);
            }
        };

        Runnable clearForm = () -> {
            personId.clear();
            salary.clear();
            hireDate.setValue(null);
            table.getSelectionModel().clearSelection();
        };

        btnRefresh.setOnAction(e -> refresh.run());
        btnClear.setOnAction(e -> clearForm.run());

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldV, a) -> {
            if (a == null) return;
            personId.setText(String.valueOf(a.personId()));
            salary.setText(a.salary() == null ? "" : a.salary().toPlainString());
            hireDate.setValue(a.hireDate());
        });

        btnCreate.setOnAction(e -> {
            try {
                Long pid = parseLong(personId.getText(), "Person ID");
                if (pid == null) return;

                BigDecimal s = parseBigDecimalOrNull(salary.getText(), "Salary");
                LocalDate d = hireDate.getValue();

                dao.insert(new Agent(pid, s, d));
                refresh.run();
                clearForm.run();
            } catch (SQLException ex) {
                showError("DB error while creating agent", ex);
            }
        });

        btnUpdate.setOnAction(e -> {
            Agent selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showInfo("Select an agent first.");
                return;
            }
            try {
                Long pid = parseLong(personId.getText(), "Person ID");
                if (pid == null) return;

                BigDecimal s = parseBigDecimalOrNull(salary.getText(), "Salary");
                LocalDate d = hireDate.getValue();

                dao.update(new Agent(pid, s, d));
                refresh.run();
            } catch (SQLException ex) {
                showError("DB error while updating agent", ex);
            }
        });

        btnDelete.setOnAction(e -> {
            Agent selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showInfo("Select an agent first.");
                return;
            }
            if (!confirm("Delete agent (person_id=" + selected.personId() + ")?")) return;

            try {
                dao.delete(selected.personId());
                refresh.run();
                clearForm.run();
            } catch (SQLException ex) {
                showError("DB error while deleting agent", ex);
            }
        });

        refresh.run();

        Tab tab = new Tab("Agents", root);
        tab.setClosable(false);
        return tab;
    }

    // ---- helpers ----
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
