package org.example.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        TabPane tabs = new TabPane(
                PeopleTab.build(),
                ClientTab.build(),
                AgentTab.build(),
                PropertyTab.build(),
                DealsTab.build(),
                ListingTab.build(),
                PreferencesTab.build(),
                PropertyOwnerTab.build(),
                QueriesTab.build()
        );

        stage.setTitle("Real Estate App");
        stage.setScene(new Scene(tabs, 1000, 600));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}