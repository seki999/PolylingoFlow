package com.polylingoflow.ui;

import javafx.application.Platform;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.util.Objects;

/**
 * Manages the application's system tray icon and menu.
 * Note: This uses java.awt classes, which might have integration
 * challenges on some platforms.
 */
public class TrayController {

    private static final Logger log = LoggerFactory.getLogger(TrayController.class);
    private final Stage stage;

    public TrayController(Stage stage) {
        this.stage = stage;
    }

    public void createTrayIcon() {
        if (!SystemTray.isSupported()) {
            log.warn("SystemTray is not supported on this platform.");
            return;
        }

        // TODO: Add a 16x16 icon to your resources folder
        // Image image = ImageIO.read(Objects.requireNonNull(getClass().getResource("/icons/app_icon.png")));

        // PopupMenu setup
        PopupMenu popup = new PopupMenu();
        MenuItem showItem = new MenuItem("Show App");
        showItem.addActionListener(e -> Platform.runLater(stage::show));
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.addActionListener(e -> {
            Platform.exit();
            System.exit(0);
        });

        // TODO: Create and add the TrayIcon to the SystemTray
    }
}