package org.example.ui;

import javafx.beans.property.SimpleLongProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.example.dao.PropertyOwnerDao;
import org.example.model.PropertyOwner;

import java.sql.SQLException;

public final class PropertyOwnerTab {

    private PropertyOwnerTab() {}

    public static Tab build() {
        var dao = new PropertyOwnerDao();

        TableView<PropertyOwner> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<PropertyOwner, Long> colPerson = new TableColumn<>("Person ID");
        colPerson.setCellValueFactory(c -> new SimpleLongProperty(c.getValue().personId()).asObject());

        TableColumn<PropertyOwner, Long> colProperty = new TableColumn<>("Property ID");
        colProperty.setCellValueFactory(c -> new SimpleLongProperty(c.getValue().propertyId()).asObject());

        table.getColumns().addAll(colPerson, colProperty);

        // Form
        TextField personId = new TextField();
        TextField propertyId = new TextField();

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.addRow(0, new Label("Person ID"), personId);
        form.addRow(1, new Label("Property ID"), propertyId);

        // Buttons
        Button btnRefresh = new Button("Refresh");
        Button btnClear   = new Button("Clear");
        Button btnCreate  = new Button("Create");
        Button btnUpdate  = new Button("Update");
        Button btnDelete  = new Button("Delete");

        HBox buttons = new HBox(10, btnRefresh, btnClear, btnCreate, btnUpdate, btnDelete);

        VBox right = new VBox(12, form, buttons);
        right.setPadding(new Insets(10));
        right.setPrefWidth(380);

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        root.setCenter(table);
        root.setRight(right);

        Runnable refresh = () -> {
            try {
                table.setItems(FXCollections.observableArrayList(dao.findAll()));
            } catch (SQLException ex) {
                showError("DB error while loading property owners", ex);
            }
        };

        Runnable clearForm = () -> {
            personId.clear();
            propertyId.clear();
            table.getSelectionModel().clearSelection();
        };

        btnRefresh.setOnAction(e -> refresh.run());
        btnClear.setOnAction(e -> clearForm.run());

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldV, po) -> {
            if (po == null) return;
            personId.setText(String.valueOf(po.personId()));
            propertyId.setText(String.valueOf(po.propertyId()));
        });

        btnCreate.setOnAction(e -> {
            Long pid = parseLong(personId.getText(), "Person ID");
            Long propId = parseLong(propertyId.getText(), "Property ID");
            if (pid == null || propId == null) return;

            try {
                dao.insert(new PropertyOwner(pid, propId));
                refresh.run();
                clearForm.run();
            } catch (SQLException ex) {
                showError("DB error while creating property owner link", ex);
            }
        });

        btnUpdate.setOnAction(e -> {
            PropertyOwner selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showInfo("Select a row first.");
                return;
            }

            Long pid = parseLong(personId.getText(), "Person ID");
            Long propId = parseLong(propertyId.getText(), "Property ID");
            if (pid == null || propId == null) return;

            try {
                dao.update(selected.personId(), selected.propertyId(), new PropertyOwner(pid, propId));
                refresh.run();
            } catch (SQLException ex) {
                showError("DB error while updating property owner link", ex);
            }
        });

        btnDelete.setOnAction(e -> {
            PropertyOwner selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showInfo("Select a row first.");
                return;
            }

            if (!confirm("Delete link person_id=" + selected.personId() + " ↔ property_id=" + selected.propertyId() + "?"))
                return;

            try {
                dao.delete(selected.personId(), selected.propertyId());
                refresh.run();
                clearForm.run();
            } catch (SQLException ex) {
                // you wanted: try delete, if blocked -> warn only
                showInfo("Cannot delete this link.\nIt is referenced elsewhere.");
            }
        });

        refresh.run();

        Tab tab = new Tab("Property Owners", root);
        tab.setClosable(false);
        return tab;
    }

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
