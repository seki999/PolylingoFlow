package com.polylingoflow.ui;

import com.polylingoflow.bridge.Bridge;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The controller for the main UI (MainView.fxml).
 * It handles user interactions and binds UI components to the application's state (Bridge).
 */
public class MainController {

    private static final Logger log = LoggerFactory.getLogger(MainController.class);

    @FXML
    private TextArea originalTextArea;

    @FXML
    private TextArea translatedTextArea;

    @FXML
    private Button startStopButton;

    private boolean isRecording = false;

    @FXML
    public void initialize() {
        // Bind the text areas to the properties in the Bridge
        originalTextArea.textProperty().bind(Bridge.getInstance().transcribedTextProperty());
        translatedTextArea.textProperty().bind(Bridge.getInstance().translatedTextProperty());
        log.info("MainController initialized and UI components are bound.");
    }

    @FXML
    private void onStartStopClick() {
        isRecording = !isRecording;
        startStopButton.setText(isRecording ? "Stop Recording" : "Start Recording");
        log.info("Start/Stop button clicked. isRecording: {}", isRecording);
        // TODO: Add logic to start/stop AudioCapture, WhisperEngine, etc.
    }

    @FXML
    private void onExportClick() {
        log.info("Export button clicked.");
        // TODO: Add logic to call the Exporter module.
    }
}