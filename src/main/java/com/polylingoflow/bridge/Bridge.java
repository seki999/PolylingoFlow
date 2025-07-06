package com.polylingoflow.bridge;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * 应用程序的中央状态管理类。
 * 它充当后端服务（音频、Whisper、翻译）和UI之间的桥梁。
 * 它使用JavaFX属性，以便与UI轻松进行数据绑定。
 * 此类是一个单例，以确保应用程序状态的唯一真实来源。
 */
public class Bridge {

    private static final Bridge INSTANCE = new Bridge();

    // --- UI 状态属性 ---

    // 用于保存Whisper原始转录文本的属性
    private final StringProperty transcribedText = new SimpleStringProperty("");

    // 用于保存翻译后文本的属性
    private final StringProperty translatedText = new SimpleStringProperty("");

    // 用于显示应用程序当前状态的属性 (例如, "正在聆听...", "正在转录...")
    private final StringProperty statusText = new SimpleStringProperty("准备就绪");

    // 用于控制和反映录音状态的属性
    private final BooleanProperty recording = new SimpleBooleanProperty(false);


    private Bridge() {
        // 私有构造函数以强制执行单例模式
    }

    /**
     * @return Bridge的单例实例。
     */
    public static Bridge getInstance() {
        return INSTANCE;
    }

    // --- 用于UI绑定的属性访问器 ---

    public StringProperty transcribedTextProperty() {
        return transcribedText;
    }

    public StringProperty translatedTextProperty() {
        return translatedText;
    }

    public StringProperty statusTextProperty() {
        return statusText;
    }

    public BooleanProperty recordingProperty() {
        return recording;
    }

    // --- 用于后端服务的状态修改器 ---

    /**
     * 更新转录的文本。此方法是线程安全的，可以从任何后端线程调用。
     *
     * @param text 新的转录文本。
     */
    public void setTranscribedText(String text) {
        // 确保UI更新在JavaFX应用程序线程上运行
        Platform.runLater(() -> transcribedText.set(text));
    }

    /**
     * 更新翻译后的文本。此方法是线程安全的。
     *
     * @param text 新的翻译文本。
     */
    public void setTranslatedText(String text) {
        Platform.runLater(() -> translatedText.set(text));
    }

    /**
     * 更新应用程序状态消息。此方法是线程安全的。
     *
     * @param text 新的状态消息。
     */
    public void setStatusText(String text) {
        Platform.runLater(() -> statusText.set(text));
    }

    /**
     * 设置录音状态。此方法是线程安全的。
     *
     * @param isRecording 新的录音状态。
     */
    public void setRecording(boolean isRecording) {
        Platform.runLater(() -> recording.set(isRecording));
    }
}