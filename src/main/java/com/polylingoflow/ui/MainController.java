package com.polylingoflow.ui;

import com.polylingoflow.bridge.Bridge;
import com.polylingoflow.export.Exporter;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * 主UI（MainView.fxml）的控制器。
 * 它处理用户交互，并将UI组件绑定到应用程序的状态（Bridge）。
 */
public class MainController {

    private static final Logger log = LoggerFactory.getLogger(MainController.class);

    // 假设你的 FXML 中有一个用于显示状态的 Label
    @FXML
    private Label statusLabel;

    @FXML
    private TextArea originalTextArea;

    @FXML
    private TextArea translatedTextArea;

    @FXML
    private Button startStopButton;

    @FXML
    private Button exportButton; // 假设你有一个导出按钮

    private final Bridge bridge = Bridge.getInstance();
    private final Exporter exporter = new Exporter();

    /**
     * 当 FXML 文件加载后，此方法会自动调用。
     * 这是设置所有数据绑定和初始状态的最佳位置。
     */
    @FXML
    public void initialize() {
        // 将UI组件的属性绑定到Bridge中的属性
        // 这是单向绑定，UI会随着Bridge中的数据变化而自动更新
        originalTextArea.textProperty().bind(bridge.transcribedTextProperty());
        translatedTextArea.textProperty().bind(bridge.translatedTextProperty());
        statusLabel.textProperty().bind(bridge.statusTextProperty());

        // 使用Bindings API来根据录音状态动态改变按钮文本
        // 当 bridge.recordingProperty() 为 true 时，文本为 "停止录音"
        // 当为 false 时，文本为 "开始录音"
        startStopButton.textProperty().bind(
                Bindings.when(bridge.recordingProperty())
                        .then("停止录音")
                        .otherwise("开始录音")
        );

        // 导出按钮只有在有内容可导出时才可用
        exportButton.disableProperty().bind(bridge.transcribedTextProperty().isEmpty());

        log.info("MainController 初始化完成，UI组件已绑定。");
    }

    /**
     * 当开始/停止按钮被点击时调用。
     * 此方法不管理任何状态，它只通知Bridge用户的意图。
     */
    @FXML
    private void onStartStopClick() {
        // 获取Bridge中的当前录音状态
        boolean isCurrentlyRecording = bridge.recordingProperty().get();
        // 设置为相反的状态。这将触发监听此属性的任何服务（如AudioCapture）
        bridge.setRecording(!isCurrentlyRecording);
        log.info("Start/Stop 按钮被点击。请求将录音状态设置为: {}", !isCurrentlyRecording);
        // 注意：实际的录音启动/停止逻辑不在这里，而是在监听Bridge属性变化的服务中。
        // 这保持了控制器（UI层）和业务逻辑层的分离。
    }

    /**
     * 当导出按钮被点击时调用。
     */
    @FXML
    private void onExportClick() {
        log.info("导出按钮被点击。");

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("导出转录结果");
        fileChooser.setInitialFileName("transcription.txt");

        // 设置文件类型过滤器
        FileChooser.ExtensionFilter txtFilter = new FileChooser.ExtensionFilter("文本文档 (*.txt)", "*.txt");
        // TODO: 当你可以生成带时间戳的片段时，可以启用这些过滤器
        // FileChooser.ExtensionFilter srtFilter = new FileChooser.ExtensionFilter("SRT 字幕 (*.srt)", "*.srt");
        // FileChooser.ExtensionFilter vttFilter = new FileChooser.ExtensionFilter("WebVTT 字幕 (*.vtt)", "*.vtt");
        fileChooser.getExtensionFilters().add(txtFilter);

        // 显示保存文件对话框
        File file = fileChooser.showSaveDialog(exportButton.getScene().getWindow());

        if (file != null) {
            try {
                // 目前，我们只导出纯文本
                String contentToExport = bridge.transcribedTextProperty().get();
                exporter.exportAsTxt(contentToExport, file.toPath());

                // 向用户显示成功消息
                showAlert(Alert.AlertType.INFORMATION, "导出成功", "文件已成功保存到:\n" + file.getAbsolutePath());
                log.info("文件成功导出到 {}", file.getAbsolutePath());

            } catch (IOException e) {
                // 向用户显示错误消息
                showAlert(Alert.AlertType.ERROR, "导出失败", "无法写入文件:\n" + e.getMessage());
                log.error("导出文件时发生错误", e);
            }
        } else {
            log.info("用户取消了导出操作。");
        }
    }

    /**
     * 一个辅助方法，用于显示一个信息或错误对话框。
     * @param alertType 对话框类型 (INFORMATION, ERROR, etc.)
     * @param title     对话框标题
     * @param message   要显示的消息
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null); // 我们不需要复杂的头部文本
        alert.setContentText(message);
        alert.showAndWait();
    }
}