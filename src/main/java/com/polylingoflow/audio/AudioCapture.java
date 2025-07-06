package com.polylingoflow.audio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Handles audio recording from the microphone using javax.sound.sampled.
 * It captures PCM audio data and passes it to a consumer for further processing (e.g., VAD).
 */
public class AudioCapture {

    private static final Logger log = LoggerFactory.getLogger(AudioCapture.class);
    // 使用守护线程工厂，这样在主程序退出时不会因为这个线程而阻塞
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "audio-capture-thread");
        t.setDaemon(true);
        return t;
    });

    private volatile boolean isRecording = false;
    private TargetDataLine targetDataLine;

    /**
     * Starts capturing audio from the default microphone.
     * The audio format is set to 16kHz, 16-bit, mono, signed PCM, which is common for speech recognition.
     *
     * @param audioConsumer A consumer that will receive the captured audio chunks (byte arrays).
     */
    public void startRecording(Consumer<byte[]> audioConsumer) {
        if (isRecording) {
            log.warn("Recording is already in progress.");
            return;
        }

        executor.submit(() -> {
            try {
                // 为语音识别定义通用音频格式 (16kHz, 16-bit, mono, PCM_SIGNED)
                AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
                DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

                if (!AudioSystem.isLineSupported(info)) {
                    log.error("Audio line for format {} is not supported. Please check your microphone.", format);
                    return;
                }

                targetDataLine = (TargetDataLine) AudioSystem.getLine(info);
                // 打开数据行并指定内部缓冲区大小，例如1秒的音频数据
                // 16000 samples/sec * 2 bytes/sample = 32000 bytes/sec
                targetDataLine.open(format, 32000);
                targetDataLine.start();
                isRecording = true;
                log.info("Started audio recording.");

                // 创建缓冲区以读取音频块，例如每次读取100毫秒的数据
                // 16000 samples/sec * 2 bytes/sample * 0.1 sec = 3200 bytes
                byte[] buffer = new byte[3200];
                while (isRecording) {
                    int bytesRead = targetDataLine.read(buffer, 0, buffer.length);
                    if (bytesRead > 0) {
                        // 创建一个缓冲区副本并传递给消费者
                        // 这对于防止消费者处理正在被覆盖的缓冲区至关重要
                        final byte[] data = new byte[bytesRead];
                        System.arraycopy(buffer, 0, data, 0, bytesRead);
                        audioConsumer.accept(data);
                    }
                }
            } catch (LineUnavailableException e) {
                log.error("Audio line is unavailable. It might be in use by another application.", e);
            } finally {
                // 确保在录音停止或发生错误时正确关闭数据行
                if (targetDataLine != null && targetDataLine.isOpen()) {
                    targetDataLine.stop();
                    targetDataLine.close();
                    log.info("Audio capture thread finished and line closed.");
                }
            }
        });
    }

    /**
     * Stops the audio capture gracefully.
     */
    public void stopRecording() {
        if (!isRecording) {
            // 如果已经停止，这通常不是一个警告，所以使用 debug 级别
            log.debug("Recording is not in progress or already stopped.");
            return;
        }
        log.info("Attempting to stop audio recording...");
        isRecording = false; // 向录音线程发送停止信号

        // 录音线程将在其 finally 块中处理 TargetDataLine 的关闭
        // 我们只需要关闭 executor
        executor.shutdown();
        try {
            // 等待录音线程优雅地完成其当前工作和清理
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                log.warn("Executor did not terminate in the specified time. Forcing shutdown.");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("Interrupted while waiting for recording to stop.", e);
            executor.shutdownNow();
            // 保持中断状态
            Thread.currentThread().interrupt();
        }
        log.info("Audio recording stopped successfully.");
    }
}