package org.example.ui;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.example.dao.SuccessfulDealDao;
import org.example.model.SuccessfulDeal;

import java.math.BigDecimal;
import java.sql.SQLException;

public final class DealsTab {

    private DealsTab() {}

    public static Tab build() {
        var dao = new SuccessfulDealDao();

        TableView<SuccessfulDeal> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<SuccessfulDeal, Long> colDealId = new TableColumn<>("Deal ID");
        colDealId.setCellValueFactory(c ->
                new SimpleLongProperty(c.getValue().dealId()).asObject()
        );

        TableColumn<SuccessfulDeal, Long> colPropertyId = new TableColumn<>("Property ID");
        colPropertyId.setCellValueFactory(c ->
                new SimpleLongProperty(c.getValue().propertyId()).asObject()
        );

        TableColumn<SuccessfulDeal, String> colFinalPrice = new TableColumn<>("Final price");
        colFinalPrice.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().finalPrice() == null ? "" : c.getValue().finalPrice().toPlainString())
        );

        TableColumn<SuccessfulDeal, Long> colAgentId = new TableColumn<>("Agent ID");
        colAgentId.setCellValueFactory(c ->
                new SimpleLongProperty(c.getValue().agentId()).asObject()
        );

        TableColumn<SuccessfulDeal, Long> colClientId = new TableColumn<>("Client ID");
        colClientId.setCellValueFactory(c ->
                new SimpleLongProperty(c.getValue().clientId()).asObject()
        );

        table.getColumns().addAll(colDealId, colPropertyId, colFinalPrice, colAgentId, colClientId);

        // Form
        TextField dealId = new TextField();
        dealId.setDisable(true);

        TextField propertyId = new TextField();
        TextField finalPrice = new TextField();
        TextField agentId = new TextField();
        TextField clientId = new TextField();

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.addRow(0, new Label("Deal ID"), dealId);
        form.addRow(1, new Label("Property ID"), propertyId);
        form.addRow(2, new Label("Final price"), finalPrice);
        form.addRow(3, new Label("Agent ID"), agentId);
        form.addRow(4, new Label("Client ID"), clientId);

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
                showError("DB error while loading deals", ex);
            }
        };

        Runnable clearForm = () -> {
            dealId.clear();
            propertyId.clear();
            finalPrice.clear();
            agentId.clear();
            clientId.clear();
            table.getSelectionModel().clearSelection();
        };

        btnRefresh.setOnAction(e -> refresh.run());
        btnClear.setOnAction(e -> clearForm.run());

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldV, d) -> {
            if (d == null) return;
            dealId.setText(String.valueOf(d.dealId()));
            propertyId.setText(String.valueOf(d.propertyId()));
            finalPrice.setText(d.finalPrice() == null ? "" : d.finalPrice().toPlainString());
            agentId.setText(String.valueOf(d.agentId()));
            clientId.setText(String.valueOf(d.clientId()));
        });

        btnCreate.setOnAction(e -> {
            try {
                Long pid = parseLong(propertyId.getText(), "Property ID");
                Long aid = parseLong(agentId.getText(), "Agent ID");
                Long cid = parseLong(clientId.getText(), "Client ID");
                if (pid == null || aid == null || cid == null) return;

                BigDecimal fp = parseBigDecimalOrNull(finalPrice.getText(), "Final price");
                if (fp == null && !finalPrice.getText().trim().isEmpty()) return;

                dao.insert(new SuccessfulDeal(0, pid, fp, aid, cid));
                refresh.run();
                clearForm.run();
            } catch (SQLException ex) {
                showError("DB error while creating deal", ex);
            }
        });

        btnUpdate.setOnAction(e -> {
            SuccessfulDeal selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showInfo("Select a deal first.");
                return;
            }
            try {
                Long pid = parseLong(propertyId.getText(), "Property ID");
                Long aid = parseLong(agentId.getText(), "Agent ID");
                Long cid = parseLong(clientId.getText(), "Client ID");
                if (pid == null || aid == null || cid == null) return;

                BigDecimal fp = parseBigDecimalOrNull(finalPrice.getText(), "Final price");
                if (fp == null && !finalPrice.getText().trim().isEmpty()) return;

                dao.update(new SuccessfulDeal(selected.dealId(), pid, fp, aid, cid));
                refresh.run();
            } catch (SQLException ex) {
                showError("DB error while updating deal", ex);
            }
        });

        btnDelete.setOnAction(e -> {
            SuccessfulDeal selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showInfo("Select a deal first.");
                return;
            }
            if (!confirm("Delete deal " + selected.dealId() + "?")) return;

            try {
                dao.delete(selected.dealId());
                refresh.run();
                clearForm.run();
            } catch (SQLException ex) {
                showError("DB error while deleting deal", ex);
            }
        });

        refresh.run();

        Tab tab = new Tab("Deals", root);
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

