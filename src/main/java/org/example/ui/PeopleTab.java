package org.example.ui;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import org.example.dao.PersonDao;
import org.example.model.Person;

import java.sql.SQLException;

public final class PeopleTab {

    private PeopleTab() {}

    public static Tab build() {
        var dao = new PersonDao();

        // Table
        TableView<Person> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Person, Long> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c ->
                new SimpleLongProperty(c.getValue().personId()).asObject()
        );

        TableColumn<Person, String> colFirst = new TableColumn<>("First name");
        colFirst.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().firstName())
        );

        TableColumn<Person, String> colLast = new TableColumn<>("Last name");
        colLast.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().lastName())
        );

        TableColumn<Person, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().email())
        );

        TableColumn<Person, String> colPhone = new TableColumn<>("Phone");
        colPhone.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().phoneNumber())
        );

        table.getColumns().addAll(colId, colFirst, colLast, colEmail, colPhone);

        // Form
        TextField firstName = new TextField();
        TextField lastName  = new TextField();
        TextField email     = new TextField();
        TextField phone     = new TextField();

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.addRow(0, new Label("First name"), firstName);
        form.addRow(1, new Label("Last name"), lastName);
        form.addRow(2, new Label("Email"), email);
        form.addRow(3, new Label("Phone"), phone);

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

        // Helpers
        Runnable refresh = () -> {
            try {
                table.setItems(FXCollections.observableArrayList(dao.findAll()));
            } catch (SQLException ex) {
                showError("DB error while loading persons", ex);
            }
        };

        Runnable clearForm = () -> {
            firstName.clear();
            lastName.clear();
            email.clear();
            phone.clear();
            table.getSelectionModel().clearSelection();
        };

        // Events
        btnRefresh.setOnAction(e -> refresh.run());
        btnClear.setOnAction(e -> clearForm.run());

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldV, p) -> {
            if (p == null) return;
            firstName.setText(p.firstName());
            lastName.setText(p.lastName());
            email.setText(p.email());
            phone.setText(p.phoneNumber());
        });

        btnCreate.setOnAction(e -> {
            try {
                Person toInsert = new Person(0,
                        firstName.getText().trim(),
                        lastName.getText().trim(),
                        email.getText().trim(),
                        phone.getText().trim()
                );
                dao.insert(toInsert);
                refresh.run();
                clearForm.run();
            } catch (SQLException ex) {
                showError("DB error while creating person", ex);
            }
        });

        btnUpdate.setOnAction(e -> {
            Person selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showInfo("Select a person first.");
                return;
            }
            try {
                Person updated = new Person(selected.personId(),
                        firstName.getText().trim(),
                        lastName.getText().trim(),
                        email.getText().trim(),
                        phone.getText().trim()
                );
                dao.update(updated);
                refresh.run();
            } catch (SQLException ex) {
                showError("DB error while updating person", ex);
            }
        });

        btnDelete.setOnAction(e -> {
            Person selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showInfo("Select a person first.");
                return;
            }

            if (!confirm("Delete person " + selected.personId() + "?")) return;

            try {
                String blockers = dao.getDeleteBlockers(selected.personId());

                if (!blockers.isBlank()) {
                    showInfo("Cannot delete this person.\nReferenced in: " + blockers);
                    return;
                }

                dao.delete(selected.personId());
                refresh.run();
                clearForm.run();

            } catch (SQLException ex) {
                showError("DB error while deleting person", ex);
            }
        });

        // initial load
        refresh.run();

        Tab tab = new Tab("People", root);
        tab.setClosable(false);
        return tab;
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

