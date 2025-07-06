package com.polylingoflow.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

/**
 * The main UI class that sets up the JavaFX stage and scene.
 * It loads the user interface from an FXML file.
 */
public class MainUI extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/MainView.fxml")));
            Scene scene = new Scene(root, 800, 600);
            primaryStage.setTitle("PolylingoFlow");
            primaryStage.setScene(scene);
            primaryStage.show();

            // TODO: Initialize TrayController here
            // TrayController trayController = new TrayController(primaryStage);
            // trayController.createTrayIcon();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}