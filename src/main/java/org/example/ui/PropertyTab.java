package org.example.ui;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.example.dao.PropertyDao;
import org.example.model.Property;

import java.math.BigDecimal;
import java.sql.SQLException;

public final class PropertyTab {

    private PropertyTab() {}

    public static Tab build() {
        var dao = new PropertyDao();

        TableView<Property> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Property, Long> colId = new TableColumn<>("Property ID");
        colId.setCellValueFactory(c ->
                new SimpleLongProperty(c.getValue().propertyId()).asObject()
        );

        TableColumn<Property, String> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(c -> new SimpleStringProperty(nvl(c.getValue().propertyType())));

        TableColumn<Property, String> colPrice = new TableColumn<>("Price");
        colPrice.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().price() == null ? "" : c.getValue().price().toPlainString())
        );

        TableColumn<Property, String> colSqm = new TableColumn<>("Sqm");
        colSqm.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().squareMeters() == null ? "" : c.getValue().squareMeters().toPlainString())
        );

        TableColumn<Property, String> colCity = new TableColumn<>("City");
        colCity.setCellValueFactory(c -> new SimpleStringProperty(nvl(c.getValue().city())));

        TableColumn<Property, String> colOwner = new TableColumn<>("Owner ID");
        colOwner.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().ownerId() == null ? "" : String.valueOf(c.getValue().ownerId()))
        );

        table.getColumns().addAll(colId, colType, colPrice, colSqm, colCity, colOwner);

        // Form
        TextField propertyId = new TextField();
        propertyId.setDisable(true); // identity, not editable

        ComboBox<String> propertyType = new ComboBox<>(FXCollections.observableArrayList("garage", "house", "apartment"));
        propertyType.setEditable(false);

        TextField price = new TextField();
        TextField squareMeters = new TextField();
        TextField latitude = new TextField();
        TextField longitude = new TextField();
        TextField city = new TextField();

        TextField ownerId = new TextField();
        ownerId.setPromptText("person_id (optional)");

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);

        form.addRow(0, new Label("Property ID"), propertyId);
        form.addRow(1, new Label("Type"), propertyType);
        form.addRow(2, new Label("Price"), price);
        form.addRow(3, new Label("Square meters"), squareMeters);
        form.addRow(4, new Label("Latitude"), latitude);
        form.addRow(5, new Label("Longitude"), longitude);
        form.addRow(6, new Label("City"), city);
        form.addRow(7, new Label("Owner ID"), ownerId);

        Button btnRefresh = new Button("Refresh");
        Button btnClear   = new Button("Clear");
        Button btnCreate  = new Button("Create");
        Button btnUpdate  = new Button("Update");
        Button btnDelete  = new Button("Delete");

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
                showError("DB error while loading properties", ex);
            }
        };

        Runnable clearForm = () -> {
            propertyId.clear();
            propertyType.getSelectionModel().clearSelection();
            price.clear();
            squareMeters.clear();
            latitude.clear();
            longitude.clear();
            city.clear();
            ownerId.clear();
            table.getSelectionModel().clearSelection();
        };

        btnRefresh.setOnAction(e -> refresh.run());
        btnClear.setOnAction(e -> clearForm.run());

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldV, p) -> {
            if (p == null) return;
            propertyId.setText(String.valueOf(p.propertyId()));
            propertyType.setValue(p.propertyType());
            price.setText(p.price() == null ? "" : p.price().toPlainString());
            squareMeters.setText(p.squareMeters() == null ? "" : p.squareMeters().toPlainString());
            latitude.setText(nvl(p.latitude()));
            longitude.setText(nvl(p.longitude()));
            city.setText(nvl(p.city()));
            ownerId.setText(p.ownerId() == null ? "" : String.valueOf(p.ownerId()));
        });

        btnCreate.setOnAction(e -> {
            try {
                String type = propertyType.getValue();
                if (type == null || type.isBlank()) {
                    showInfo("Property type is required.");
                    return;
                }

                BigDecimal pr = parseBigDecimalOrNull(price.getText(), "Price");
                if (pr == null && !price.getText().trim().isEmpty()) return;

                BigDecimal sqm = parseBigDecimalOrNull(squareMeters.getText(), "Square meters");
                if (sqm == null && !squareMeters.getText().trim().isEmpty()) return;

                Long oid = parseLongOrNull(ownerId.getText(), "Owner ID");
                if (oid == null && !ownerId.getText().trim().isEmpty()) return;

                Property toInsert = new Property(
                        0,
                        pr,
                        sqm,
                        latitude.getText().trim(),
                        longitude.getText().trim(),
                        city.getText().trim(),
                        type,
                        oid
                );

                dao.insert(toInsert);
                refresh.run();
                clearForm.run();
            } catch (SQLException ex) {
                showError("DB error while creating property", ex);
            }
        });

        btnUpdate.setOnAction(e -> {
            Property selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showInfo("Select a property first.");
                return;
            }
            try {
                String type = propertyType.getValue();
                if (type == null || type.isBlank()) {
                    showInfo("Property type is required.");
                    return;
                }

                BigDecimal pr = parseBigDecimalOrNull(price.getText(), "Price");
                if (pr == null && !price.getText().trim().isEmpty()) return;

                BigDecimal sqm = parseBigDecimalOrNull(squareMeters.getText(), "Square meters");
                if (sqm == null && !squareMeters.getText().trim().isEmpty()) return;

                Long oid = parseLongOrNull(ownerId.getText(), "Owner ID");
                if (oid == null && !ownerId.getText().trim().isEmpty()) return;

                Property updated = new Property(
                        selected.propertyId(),
                        pr,
                        sqm,
                        latitude.getText().trim(),
                        longitude.getText().trim(),
                        city.getText().trim(),
                        type,
                        oid
                );

                dao.update(updated);
                refresh.run();
            } catch (SQLException ex) {
                showError("DB error while updating property", ex);
            }
        });

        btnDelete.setOnAction(e -> {
            Property selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showInfo("Select a property first.");
                return;
            }
            if (!confirm("Delete property " + selected.propertyId() + "?")) return;

            try {
                dao.delete(selected.propertyId());
                refresh.run();
                clearForm.run();
            } catch (SQLException ex) {
                showError("DB error while deleting property", ex);
            }
        });

        refresh.run();

        Tab tab = new Tab("Properties", root);
        tab.setClosable(false);
        return tab;
    }

    // ---- helpers ----
    private static String nvl(String s) { return s == null ? "" : s; }

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

    private static Long parseLongOrNull(String s, String field) {
        String t = s.trim();
        if (t.isEmpty()) return null;
        try {
            return Long.parseLong(t);
        } catch (NumberFormatException ex) {
            showInfo(field + " must be a whole number (or blank).");
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

