package com.polylingoflow.bridge;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * The central state management class for the application.
 * It acts as a bridge between the backend services (audio, whisper, translation)
 * and the UI. It uses JavaFX properties to allow for easy data binding with the UI.
 */
public class Bridge {

    private static final Bridge INSTANCE = new Bridge();

    // Property to hold the original transcribed text from Whisper
    private final StringProperty transcribedText = new SimpleStringProperty("");

    // Property to hold the translated text
    private final StringProperty translatedText = new SimpleStringProperty("");

    private Bridge() {
        // Private constructor to enforce singleton pattern
    }

    /**
     * @return The singleton instance of the Bridge.
     */
    public static Bridge getInstance() {
        return INSTANCE;
    }

    public StringProperty transcribedTextProperty() {
        return transcribedText;
    }

    public StringProperty translatedTextProperty() {
        return translatedText;
    }
}