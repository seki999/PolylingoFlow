package com.polylingoflow.audio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Handles audio recording from the microphone using javax.sound.sampled.
 * It captures PCM audio data and passes it to a consumer for further processing (e.g., VAD).
 */
public class AudioCapture {

    private static final Logger log = LoggerFactory.getLogger(AudioCapture.class);
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean isRecording = false;
    private TargetDataLine targetDataLine;

    /**
     * Starts capturing audio from the default microphone.
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
                // TODO: Define AudioFormat (e.g., 16kHz, 16-bit, mono, PCM_SIGNED)
                AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
                DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
                targetDataLine = (TargetDataLine) AudioSystem.getLine(info);
                targetDataLine.open(format);
                targetDataLine.start();
                isRecording = true;
                log.info("Started audio recording.");

                // TODO: Implement logic to read from targetDataLine in chunks and pass to audioConsumer
                // byte[] buffer = new byte[...];
                // while(isRecording) {
                //     int bytesRead = targetDataLine.read(buffer, 0, buffer.length);
                //     if (bytesRead > 0) {
                //         audioConsumer.accept(buffer);
                //     }
                // }

            } catch (LineUnavailableException e) {
                log.error("Audio line is unavailable.", e);
            }
        });
    }

    /**
     * Stops the audio capture.
     */
    public void stopRecording() {
        if (!isRecording) return;
        isRecording = false;
        if (targetDataLine != null) {
            targetDataLine.stop();
            targetDataLine.close();
            log.info("Stopped audio recording.");
        }
        executor.shutdown();
    }
}