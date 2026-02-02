package org.example.ui;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.example.dao.QueryDao;
import org.example.model.AgentSalesRow;
import org.example.model.ClientBudgetMatchRow;
import org.example.model.PropertyOwnerRow;
import org.example.model.AvgSaleByTypeRow;
import org.example.model.ClientDealsRow;
import org.example.model.UnsoldPropertyRow;

import java.sql.SQLException;

public final class QueriesTab {

    private QueriesTab() {}

    private enum QueryType {
        PROPERTIES_WITH_OPTIONAL_OWNER,
        TOP_AGENTS_BY_SALES,
        PROPERTIES_UNDER_CLIENT_BUDGET,
        AVG_SALE_PRICE_BY_TYPE,
        TOP_CLIENTS_BY_DEALS,
        UNSOLD_PROPERTIES
    }

    public static Tab build() {
        var dao = new QueryDao();

        // Controls
        ComboBox<QueryType> queryPicker = new ComboBox<>(FXCollections.observableArrayList(QueryType.values()));
        queryPicker.setValue(QueryType.PROPERTIES_WITH_OPTIONAL_OWNER);

        TextField clientId = new TextField();
        clientId.setPromptText("Client ID (for budget query)");
        clientId.setDisable(true);

        Button btnRun = new Button("Run");
        Button btnClear = new Button("Clear results");

        HBox top = new HBox(10,
                new Label("Query:"), queryPicker,
                new Label("Client ID:"), clientId,
                btnRun, btnClear
        );
        top.setPadding(new Insets(10));

        // Results area
        StackPane resultsPane = new StackPane();
        resultsPane.setPadding(new Insets(10));

        Label hint = new Label("Run a query to see results.");
        resultsPane.getChildren().add(hint);

        // Enable clientId only for relevant query
        queryPicker.valueProperty().addListener((obs, oldV, v) -> {
            clientId.setDisable(v != QueryType.PROPERTIES_UNDER_CLIENT_BUDGET);
        });

        btnClear.setOnAction(e -> resultsPane.getChildren().setAll(new Label("Run a query to see results.")));

        btnRun.setOnAction(e -> {
            QueryType qt = queryPicker.getValue();
            try {
                switch (qt) {
                    case PROPERTIES_WITH_OPTIONAL_OWNER -> {
                        var data = dao.propertiesWithOptionalOwner();
                        resultsPane.getChildren().setAll(buildPropertiesOwnerTable(data));
                    }
                    case TOP_AGENTS_BY_SALES -> {
                        var data = dao.topAgentsBySales();
                        resultsPane.getChildren().setAll(buildAgentSalesTable(data));
                    }
                    case PROPERTIES_UNDER_CLIENT_BUDGET -> {
                        Long cid = parseLong(clientId.getText(), "Client ID");
                        if (cid == null) return;
                        var data = dao.propertiesUnderClientBudget(cid);
                        resultsPane.getChildren().setAll(buildClientBudgetTable(data));
                    }
                    case AVG_SALE_PRICE_BY_TYPE -> {
                        var data = dao.avgSalePriceByPropertyType();
                        resultsPane.getChildren().setAll(buildAvgSaleByTypeTable(data));
                    }
                    case TOP_CLIENTS_BY_DEALS -> {
                        var data = dao.topClientsByDeals();
                        resultsPane.getChildren().setAll(buildTopClientsTable(data));
                    }
                    case UNSOLD_PROPERTIES -> {
                        var data = dao.unsoldProperties();
                        resultsPane.getChildren().setAll(buildUnsoldPropertiesTable(data));
                    }
                }
            } catch (SQLException ex) {
                showError("DB error while running query", ex);
            }
        });

        BorderPane root = new BorderPane();
        root.setTop(top);
        root.setCenter(resultsPane);

        Tab tab = new Tab("Queries", root);
        tab.setClosable(false);
        return tab;
    }

    // ----------------------------
    // Tables
    // ----------------------------

    private static TableView<PropertyOwnerRow> buildPropertiesOwnerTable(java.util.List<PropertyOwnerRow> rows) {
        TableView<PropertyOwnerRow> t = new TableView<>();
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<PropertyOwnerRow, Long> c1 = new TableColumn<>("Property ID");
        c1.setCellValueFactory(v -> new SimpleLongProperty(v.getValue().propertyId()).asObject());

        TableColumn<PropertyOwnerRow, String> c2 = new TableColumn<>("Type");
        c2.setCellValueFactory(v -> new SimpleStringProperty(nvl(v.getValue().propertyType())));

        TableColumn<PropertyOwnerRow, String> c3 = new TableColumn<>("City");
        c3.setCellValueFactory(v -> new SimpleStringProperty(nvl(v.getValue().city())));

        TableColumn<PropertyOwnerRow, String> c4 = new TableColumn<>("Owner ID");
        c4.setCellValueFactory(v -> new SimpleStringProperty(v.getValue().ownerId() == null ? "" : String.valueOf(v.getValue().ownerId())));

        TableColumn<PropertyOwnerRow, String> c5 = new TableColumn<>("Owner name");
        c5.setCellValueFactory(v -> new SimpleStringProperty(nvl(v.getValue().ownerName())));

        t.getColumns().addAll(c1, c2, c3, c4, c5);
        t.setItems(FXCollections.observableArrayList(rows));
        return t;
    }

    private static TableView<AgentSalesRow> buildAgentSalesTable(java.util.List<AgentSalesRow> rows) {
        TableView<AgentSalesRow> t = new TableView<>();
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<AgentSalesRow, Long> c1 = new TableColumn<>("Agent ID");
        c1.setCellValueFactory(v -> new SimpleLongProperty(v.getValue().agentId()).asObject());

        TableColumn<AgentSalesRow, String> c2 = new TableColumn<>("First name");
        c2.setCellValueFactory(v -> new SimpleStringProperty(nvl(v.getValue().firstName())));

        TableColumn<AgentSalesRow, String> c3 = new TableColumn<>("Last name");
        c3.setCellValueFactory(v -> new SimpleStringProperty(nvl(v.getValue().lastName())));

        TableColumn<AgentSalesRow, Long> c4 = new TableColumn<>("Deals");
        c4.setCellValueFactory(v -> new SimpleLongProperty(v.getValue().deals()).asObject());

        TableColumn<AgentSalesRow, String> c5 = new TableColumn<>("Total sales");
        c5.setCellValueFactory(v -> new SimpleStringProperty(v.getValue().totalSales() == null ? "0" : v.getValue().totalSales().toPlainString()));

        t.getColumns().addAll(c1, c2, c3, c4, c5);
        t.setItems(FXCollections.observableArrayList(rows));
        return t;
    }

    private static TableView<ClientBudgetMatchRow> buildClientBudgetTable(java.util.List<ClientBudgetMatchRow> rows) {
        TableView<ClientBudgetMatchRow> t = new TableView<>();
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ClientBudgetMatchRow, Long> c1 = new TableColumn<>("Client ID");
        c1.setCellValueFactory(v -> new SimpleLongProperty(v.getValue().clientId()).asObject());

        TableColumn<ClientBudgetMatchRow, String> c2 = new TableColumn<>("Client name");
        c2.setCellValueFactory(v -> new SimpleStringProperty(nvl(v.getValue().clientName())));

        TableColumn<ClientBudgetMatchRow, Long> c3 = new TableColumn<>("Property ID");
        c3.setCellValueFactory(v -> new SimpleLongProperty(v.getValue().propertyId()).asObject());

        TableColumn<ClientBudgetMatchRow, String> c4 = new TableColumn<>("City");
        c4.setCellValueFactory(v -> new SimpleStringProperty(nvl(v.getValue().city())));

        TableColumn<ClientBudgetMatchRow, String> c5 = new TableColumn<>("Type");
        c5.setCellValueFactory(v -> new SimpleStringProperty(nvl(v.getValue().propertyType())));

        TableColumn<ClientBudgetMatchRow, String> c6 = new TableColumn<>("Price");
        c6.setCellValueFactory(v -> new SimpleStringProperty(v.getValue().price() == null ? "" : v.getValue().price().toPlainString()));

        t.getColumns().addAll(c1, c2, c3, c4, c5, c6);
        t.setItems(FXCollections.observableArrayList(rows));
        return t;
    }

    private static TableView<AvgSaleByTypeRow> buildAvgSaleByTypeTable(java.util.List<AvgSaleByTypeRow> rows) {
        TableView<AvgSaleByTypeRow> t = new TableView<>();
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<AvgSaleByTypeRow, String> c1 = new TableColumn<>("Property type");
        c1.setCellValueFactory(v -> new SimpleStringProperty(nvl(v.getValue().propertyType())));

        TableColumn<AvgSaleByTypeRow, Long> c2 = new TableColumn<>("Deals");
        c2.setCellValueFactory(v -> new SimpleLongProperty(v.getValue().deals()).asObject());

        TableColumn<AvgSaleByTypeRow, String> c3 = new TableColumn<>("Avg final price");
        c3.setCellValueFactory(v -> new SimpleStringProperty(
                v.getValue().avgFinalPrice() == null ? "" : v.getValue().avgFinalPrice().toPlainString()
        ));

        t.getColumns().addAll(c1, c2, c3);
        t.setItems(FXCollections.observableArrayList(rows));
        return t;
    }

    private static TableView<ClientDealsRow> buildTopClientsTable(java.util.List<ClientDealsRow> rows) {
        TableView<ClientDealsRow> t = new TableView<>();
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ClientDealsRow, Long> c1 = new TableColumn<>("Client ID");
        c1.setCellValueFactory(v -> new SimpleLongProperty(v.getValue().clientId()).asObject());

        TableColumn<ClientDealsRow, String> c2 = new TableColumn<>("First name");
        c2.setCellValueFactory(v -> new SimpleStringProperty(nvl(v.getValue().firstName())));

        TableColumn<ClientDealsRow, String> c3 = new TableColumn<>("Last name");
        c3.setCellValueFactory(v -> new SimpleStringProperty(nvl(v.getValue().lastName())));

        TableColumn<ClientDealsRow, Long> c4 = new TableColumn<>("Deals");
        c4.setCellValueFactory(v -> new SimpleLongProperty(v.getValue().deals()).asObject());

        TableColumn<ClientDealsRow, String> c5 = new TableColumn<>("Total spent");
        c5.setCellValueFactory(v -> new SimpleStringProperty(
                v.getValue().totalSpent() == null ? "0" : v.getValue().totalSpent().toPlainString()
        ));

        t.getColumns().addAll(c1, c2, c3, c4, c5);
        t.setItems(FXCollections.observableArrayList(rows));
        return t;
    }

    private static TableView<UnsoldPropertyRow> buildUnsoldPropertiesTable(java.util.List<UnsoldPropertyRow> rows) {
        TableView<UnsoldPropertyRow> t = new TableView<>();
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<UnsoldPropertyRow, Long> c1 = new TableColumn<>("Property ID");
        c1.setCellValueFactory(v -> new SimpleLongProperty(v.getValue().propertyId()).asObject());

        TableColumn<UnsoldPropertyRow, String> c2 = new TableColumn<>("Type");
        c2.setCellValueFactory(v -> new SimpleStringProperty(nvl(v.getValue().propertyType())));

        TableColumn<UnsoldPropertyRow, String> c3 = new TableColumn<>("City");
        c3.setCellValueFactory(v -> new SimpleStringProperty(nvl(v.getValue().city())));

        TableColumn<UnsoldPropertyRow, String> c4 = new TableColumn<>("Price");
        c4.setCellValueFactory(v -> new SimpleStringProperty(
                v.getValue().price() == null ? "" : v.getValue().price().toPlainString()
        ));

        t.getColumns().addAll(c1, c2, c3, c4);
        t.setItems(FXCollections.observableArrayList(rows));
        return t;
    }


    // ----------------------------
    // Helpers
    // ----------------------------
    private static String nvl(String s) { return s == null ? "" : s; }

    private static Long parseLong(String s, String field) {
        try {
            String t = s == null ? "" : s.trim();
            if (t.isEmpty()) {
                showInfo(field + " is required for this query.");
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
}
