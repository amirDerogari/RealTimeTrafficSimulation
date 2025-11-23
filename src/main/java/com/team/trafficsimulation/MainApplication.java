package com.team.trafficsimulation;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * The {@code MainApplication} class serves as the entry point for the JavaFX application.
 * <p>
 * It handles the initialization of the primary stage (window), loads the FXML layout
 * defining the user interface, and launches the GUI.
 * </p>
 */
public class MainApplication extends Application {

    /**
     * The main entry point for all JavaFX applications.
     * <p>
     * The start method is called after the init method has returned, and after
     * the system is ready for the application to begin running.
     * </p>
     *
     * @param stage The primary stage for this application, onto which the application scene can be set.
     * @throws IOException If the FXML resource "MainView.fxml" cannot be loaded.
     */
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("MainView.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1000, 700);
        stage.setTitle("SUMO Traffic Controller");
        stage.setScene(scene);
        stage.show();
    }
}
