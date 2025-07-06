import com.polylingoflow.audio.AudioCapture;
import com.polylingoflow.audio.VadListener;
import com.polylingoflow.audio.VadProcessor;
import com.polylingoflow.bridge.Bridge;
import com.polylingoflow.whisper.WhisperEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 管理应用程序核心服务的生命周期。
 * 负责初始化、连接和优雅地关闭所有后端组件。
 */
public class ApplicationManager {

    private static final Logger log = LoggerFactory.getLogger(ApplicationManager.class);

    private final AudioCapture audioCapture;
    private final WhisperEngine whisperEngine;
    private final Bridge bridge = Bridge.getInstance();

    public ApplicationManager() {
        this.audioCapture = new AudioCapture();
        this.whisperEngine = new WhisperEngine();
    }

    /**
     * 启动所有后端服务并建立它们之间的连接。
     */
    public void start() {
        log.info("正在启动 ApplicationManager...");

        // 1. 初始化 Whisper 引擎
        // TODO: 将模型路径改为实际路径或从配置中读取
        Path modelPath = Paths.get("models/ggml-base.en.bin");
        whisperEngine.initialize(modelPath);

        // 2. 设置 VAD 处理器和监听器
        setupVad();

        // 3. 监听来自UI的录音请求
        bridge.recordingProperty().addListener((obs, wasRecording, isRecording) -> {
            if (isRecording) {
                startAudioProcessing();
            } else {
                stopAudioProcessing();
            }
        });

        log.info("ApplicationManager 启动完成。");
    }

    private void setupVad() {
        VadListener vadListener = new VadListener() {
            private final ByteArrayOutputStream speechBuffer = new ByteArrayOutputStream();

            @Override
            public void onSpeechStart() {
                bridge.setStatusText("检测到语音...");
                speechBuffer.reset();
            }

            @Override
            public void onSpeech(byte[] audioData) {
                try {
                    speechBuffer.write(audioData);
                } catch (IOException e) {
                    log.error("无法将音频数据写入缓冲区", e);
                }
            }

            @Override
            public void onSpeechEnd() {
                bridge.setStatusText("正在转录...");
                byte[] completeSpeech = speechBuffer.toByteArray();

                // 在新线程中运行转录，以避免阻塞VAD
                new Thread(() -> {
                    String transcribedText = whisperEngine.transcribe(completeSpeech);
                    bridge.setTranscribedText(transcribedText);
                    bridge.setStatusText("准备就绪");
                }).start();
            }
        };

        // TODO: 调整这些VAD参数以获得最佳性能
        VadProcessor vadProcessor = new VadProcessor(vadListener, 100, 75.0, 700);

        // 将 VAD 处理器连接到音频捕获
        audioCapture.startRecording(vadProcessor::process);
    }

    private void startAudioProcessing() {
        bridge.setStatusText("正在聆听...");
        // AudioCapture 已经在 setupVad 中通过 startRecording 启动并等待数据
        // 这里我们只需要更新UI状态
        log.info("UI请求开始录音。");
    }

    private void stopAudioProcessing() {
        bridge.setStatusText("录音已停止。");
        // 实际的停止逻辑由 AudioCapture 的关闭钩子处理
        log.info("UI请求停止录音。");
    }

    /**
     * 优雅地关闭所有服务并释放资源。
     */
    public void shutdown() {
        log.info("正在关闭 ApplicationManager...");
        if (bridge.recordingProperty().get()) {
            bridge.setRecording(false);
        }
        audioCapture.stopRecording();
        whisperEngine.close();
        log.info("ApplicationManager 关闭完成。");
    }
}