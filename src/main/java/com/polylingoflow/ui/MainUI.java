package com.polylingoflow.ui;

import com.polylingoflow.ApplicationManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

/**
 * 应用程序的主UI类，负责设置JavaFX的舞台（Stage）和场景（Scene）。
 * 它从FXML文件加载用户界面，并管理应用程序的生命周期。
 */
public class MainUI extends Application {

    private static final Logger log = LoggerFactory.getLogger(MainUI.class);
    private ApplicationManager appManager;

    /**
     * 在start()方法之前调用。
     * 这是执行非UI初始化的理想位置。
     */
    @Override
    public void init() {
        log.info("Initializing application backend services...");
        appManager = new ApplicationManager();
        appManager.start();
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            log.info("Loading FXML and setting up the stage.");
            URL fxmlUrl = Objects.requireNonNull(getClass().getResource("/fxml/MainView.fxml"),
                    "Cannot find FXML file. Make sure '/fxml/MainView.fxml' is in your resources folder.");

            Parent root = FXMLLoader.load(fxmlUrl);
            Scene scene = new Scene(root, 800, 600);

            primaryStage.setTitle("PolylingoFlow");
            primaryStage.setScene(scene);
            primaryStage.show();

            // TODO: 实现 TrayController
            // if (SystemTray.isSupported()) {
            //     TrayController trayController = new TrayController(primaryStage);
            //     trayController.createTrayIcon();
            // } else {
            //     log.warn("System tray is not supported on this platform.");
            // }

        } catch (IOException | NullPointerException e) {
            log.error("Failed to start the application UI.", e);
            // 向用户显示一个清晰的错误对话框，而不是仅仅打印堆栈跟踪
            showErrorDialog("应用程序启动失败", "无法加载用户界面。", e);
        }
    }

    /**
     * 当应用程序关闭时调用（例如，关闭主窗口）。
     * 这是执行清理工作的关键位置。
     */
    @Override
    public void stop() {
        log.info("Application is closing. Shutting down backend services.");
        if (appManager != null) {
            appManager.shutdown();
        }
        // 确保JVM完全退出
        Platform.exit();
        System.exit(0);
    }

    /**
     * 显示一个错误对话框。
     * @param title 标题
     * @param header 头部文本
     * @param e 异常，其消息将被显示
     */
    private void showErrorDialog(String title, String header, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText("错误: " + e.getMessage());
        alert.showAndWait();
    }
}