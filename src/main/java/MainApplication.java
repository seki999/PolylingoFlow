import com.polylingoflow.audio.AudioCapture;
import com.polylingoflow.audio.VadListener;
import com.polylingoflow.audio.VadProcessor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * 应用程序的主入口点。
 * 这个类演示了如何将AudioCapture和VadProcessor集成在一起，
 * 以从麦克风捕获完整的语音片段。
 */
public class MainApplication {

    public static void main(String[] args) throws InterruptedException {
        // 1. 创建一个侦听器来处理VAD事件
        VadListener vadListener = new VadListener() {
            // 使用一个字节数组输出流来动态地聚合一个完整语音片段的所有音频数据
            private final ByteArrayOutputStream speechBuffer = new ByteArrayOutputStream();

            @Override
            public void onSpeechStart() {
                System.out.println("VAD: 检测到语音开始...");
                // 在每次新的语音开始时，清空缓冲区
                speechBuffer.reset();
            }

            @Override
            public void onSpeech(byte[] audioData) {
                try {
                    // 持续将检测到的语音数据块写入缓冲区
                    speechBuffer.write(audioData);
                } catch (IOException e) {
                    // ByteArrayOutputStream 上的 write 不会真的抛出IOException，但最佳实践是处理它
                    System.err.println("无法将音频数据写入缓冲区: " + e.getMessage());
                }
            }

            @Override
            public void onSpeechEnd() {
                System.out.println("VAD: 语音结束！");
                byte[] completeSpeech = speechBuffer.toByteArray();
                System.out.println("成功捕获到 " + (completeSpeech.length / 1024) + " KB 的语音数据。");

                // TODO: 在这里，你可以将 `completeSpeech` 字节数组发送给 WhisperEngine 进行转录
                // 例如: whisperEngine.transcribe(completeSpeech);
            }
        };

        // 2. 配置并创建VadProcessor
        // 注意：这些参数可能需要根据你的麦克风和环境进行微调
        int frameMillis = 100;          // 以100毫秒的块处理音频
        double energyThreshold = 75.0;  // 能量阈值。可以从50开始尝试，然后根据效果调整
        int silenceMillis = 700;        // 700毫秒的静音被认为是语音的结束

        VadProcessor vadProcessor = new VadProcessor(vadListener, frameMillis, energyThreshold, silenceMillis);

        // 3. 启动音频捕获，并将音频数据流式传输给VAD处理器
        AudioCapture audioCapture = new AudioCapture();
        // 这里使用了方法引用 `vadProcessor::process`，它简洁地将 `AudioCapture` 的输出连接到 `VadProcessor` 的输入
        audioCapture.startRecording(vadProcessor::process);

        System.out.println("程序已启动，正在聆听... 请说话...");

        // 让程序运行30秒以进行测试。在实际应用中，你可能会有一个GUI或服务来控制启停。
        Thread.sleep(30000);

        // 优雅地停止录音
        audioCapture.stopRecording();
        System.out.println("程序结束。");
    }
}