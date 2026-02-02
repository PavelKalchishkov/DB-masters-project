package org.example.ui;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.example.dao.ListingDao;
import org.example.model.Listing;

import java.sql.SQLException;

public final class ListingTab {

    private ListingTab() {}

    public static Tab build() {
        var dao = new ListingDao();

        TableView<Listing> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Listing, Long> colId = new TableColumn<>("Listing ID");
        colId.setCellValueFactory(c -> new SimpleLongProperty(c.getValue().listingId()).asObject());

        TableColumn<Listing, String> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(c -> new SimpleStringProperty(nvl(c.getValue().typeOfListing())));

        TableColumn<Listing, String> colDesc = new TableColumn<>("Description");
        colDesc.setCellValueFactory(c -> new SimpleStringProperty(nvl(c.getValue().description())));

        TableColumn<Listing, String> colNotes = new TableColumn<>("Notes");
        colNotes.setCellValueFactory(c -> new SimpleStringProperty(nvl(c.getValue().notes())));

        table.getColumns().addAll(colId, colType, colDesc, colNotes);

        // Form
        TextField listingId = new TextField();
        listingId.setDisable(true);

        TextField type = new TextField();
        TextField description = new TextField();
        TextField notes = new TextField();

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.addRow(0, new Label("Listing ID"), listingId);
        form.addRow(1, new Label("Type"), type);
        form.addRow(2, new Label("Description"), description);
        form.addRow(3, new Label("Notes"), notes);

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
                showError("DB error while loading listings", ex);
            }
        };

        Runnable clearForm = () -> {
            listingId.clear();
            type.clear();
            description.clear();
            notes.clear();
            table.getSelectionModel().clearSelection();
        };

        btnRefresh.setOnAction(e -> refresh.run());
        btnClear.setOnAction(e -> clearForm.run());

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldV, l) -> {
            if (l == null) return;
            listingId.setText(String.valueOf(l.listingId()));
            type.setText(nvl(l.typeOfListing()));
            description.setText(nvl(l.description()));
            notes.setText(nvl(l.notes()));
        });

        btnCreate.setOnAction(e -> {
            try {
                dao.insert(new Listing(0,
                        type.getText().trim(),
                        description.getText().trim(),
                        notes.getText().trim()
                ));
                refresh.run();
                clearForm.run();
            } catch (SQLException ex) {
                showError("DB error while creating listing", ex);
            }
        });

        btnUpdate.setOnAction(e -> {
            Listing selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showInfo("Select a listing first.");
                return;
            }
            try {
                dao.update(new Listing(selected.listingId(),
                        type.getText().trim(),
                        description.getText().trim(),
                        notes.getText().trim()
                ));
                refresh.run();
            } catch (SQLException ex) {
                showError("DB error while updating listing", ex);
            }
        });

        btnDelete.setOnAction(e -> {
            Listing selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showInfo("Select a listing first.");
                return;
            }



            if (!confirm("Delete listing " + selected.listingId() + "?")) return;

            try {
                dao.delete(selected.listingId());
                refresh.run();
                clearForm.run();
            } catch (SQLException ex) {
                showInfo("Cannot delete this listing.\nIt is referenced elsewhere.");
            }
        });

        refresh.run();

        Tab tab = new Tab("Listings", root);
        tab.setClosable(false);
        return tab;
    }

    private static String nvl(String s) { return s == null ? "" : s; }

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
