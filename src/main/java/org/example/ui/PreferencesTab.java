package org.example.ui;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.example.dao.PreferenceDao;
import org.example.model.Preference;

import java.sql.SQLException;

public final class PreferencesTab {

    private PreferencesTab() {}

    public static Tab build() {
        var dao = new PreferenceDao();

        TableView<Preference> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Preference, Long> colId = new TableColumn<>("Preference ID");
        colId.setCellValueFactory(c -> new SimpleLongProperty(c.getValue().preferenceId()).asObject());

        TableColumn<Preference, Long> colClient = new TableColumn<>("Client ID");
        colClient.setCellValueFactory(c -> new SimpleLongProperty(c.getValue().clientId()).asObject());

        TableColumn<Preference, String> colType = new TableColumn<>("Preference");
        colType.setCellValueFactory(c -> new SimpleStringProperty(nvl(c.getValue().preferenceType())));

        table.getColumns().addAll(colId, colClient, colType);

        // Form
        TextField preferenceId = new TextField();
        preferenceId.setDisable(true);

        TextField clientId = new TextField();
        clientId.setPromptText("existing client person_id");

        TextField preferenceType = new TextField();
        preferenceType.setPromptText("e.g. near metro / 2 bedrooms");

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.addRow(0, new Label("Preference ID"), preferenceId);
        form.addRow(1, new Label("Client ID"), clientId);
        form.addRow(2, new Label("Preference type"), preferenceType);

        Button btnRefresh = new Button("Refresh");
        Button btnClear   = new Button("Clear");
        Button btnCreate  = new Button("Create");
        Button btnUpdate  = new Button("Update");
        Button btnDelete = new Button("Delete");

        HBox buttons = new HBox(10, btnRefresh, btnClear, btnCreate, btnUpdate, btnDelete);

        VBox right = new VBox(12, form, buttons);
        right.setPadding(new Insets(10));
        right.setPrefWidth(420);

        BorderPane root = new BorderPane(table, null, right, null, null);
        root.setPadding(new Insets(10));

        Runnable refresh = () -> {
            try {
                table.setItems(FXCollections.observableArrayList(dao.findAll()));
            } catch (SQLException ex) {
                showError("DB error while loading preferences", ex);
            }
        };

        Runnable clearForm = () -> {
            preferenceId.clear();
            clientId.clear();
            preferenceType.clear();
            table.getSelectionModel().clearSelection();
        };

        btnRefresh.setOnAction(e -> refresh.run());
        btnClear.setOnAction(e -> clearForm.run());

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldV, p) -> {
            if (p == null) return;
            preferenceId.setText(String.valueOf(p.preferenceId()));
            clientId.setText(String.valueOf(p.clientId()));
            preferenceType.setText(nvl(p.preferenceType()));
        });

        btnCreate.setOnAction(e -> {
            Long cid = parseLong(clientId.getText(), "Client ID");
            if (cid == null) return;

            try {
                dao.insert(new Preference(0, cid, preferenceType.getText().trim()));
                refresh.run();
                clearForm.run();
            } catch (SQLException ex) {
                showError("DB error while creating preference", ex);
            }
        });

        btnUpdate.setOnAction(e -> {
            Preference selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showInfo("Select a preference first.");
                return;
            }
            Long cid = parseLong(clientId.getText(), "Client ID");
            if (cid == null) return;

            try {
                dao.update(new Preference(selected.preferenceId(), cid, preferenceType.getText().trim()));
                refresh.run();
            } catch (SQLException ex) {
                showError("DB error while updating preference", ex);
            }
        });

        btnDelete.setOnAction(e -> {
            Preference selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showInfo("Select a preference first.");
                return;
            }

            if (!confirm("Delete preference " + selected.preferenceId() + "?")) return;

            try {
                dao.delete(selected.preferenceId());
                refresh.run();
                clearForm.run();
            } catch (SQLException ex) {
                showInfo("Cannot delete this preference.");
            }
        });

        refresh.run();

        Tab tab = new Tab("Preferences", root);
        tab.setClosable(false);
        return tab;
    }

    private static String nvl(String s) { return s == null ? "" : s; }

    private static Long parseLong(String s, String field) {
        try {
            String t = s == null ? "" : s.trim();
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

