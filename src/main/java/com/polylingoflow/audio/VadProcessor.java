package com.polylingoflow.audio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;

/**
 * 一个基于能量的简单语音活动检测（VAD）处理器。
 *
 * 此实现通过计算传入音频块的能量（均方根）并将其与阈值进行比较来检测语音。
 * 它是“有状态的”，意味着它会跟踪语音和静音的周期，以识别完整的语音片段。
 *
 * 注意：这是一个适用于低噪音环境的基础实现。
 * 为了在嘈杂条件下获得稳健的性能，请考虑使用预训练的机器学习模型，
 * 如SileroVAD或WebRTC VAD（这需要JNI/JNA封装）。
 */
public class VadProcessor {

    private static final Logger log = LoggerFactory.getLogger(VadProcessor.class);

    // --- 配置参数 ---
    private final VadListener listener;
    private final double energyThreshold; // 用于语音检测的RMS能量阈值
    private final int silenceMillisThreshold; // 必须持续多长时间的静音才能触发onSpeechEnd

    // --- 内部状态 ---
    private boolean isSpeaking = false;
    private int silentFrameCount = 0;
    private final int silentFramesRequired;

    /**
     * 构造一个VadProcessor。
     *
     * @param listener               用于接收VAD事件的侦听器。
     * @param frameMillis            将要处理的音频块的持续时间（以毫秒为单位）。
     * @param energyThreshold        RMS能量阈值。高于此值的值被认为是语音。一个好的起点是50-100。
     * @param silenceMillisThreshold 标记语音片段结束所需的静音持续时间（毫秒）。
     */
    public VadProcessor(VadListener listener, int frameMillis, double energyThreshold, int silenceMillisThreshold) {
        this.listener = Objects.requireNonNull(listener, "Listener cannot be null");
        this.energyThreshold = energyThreshold;
        this.silenceMillisThreshold = silenceMillisThreshold;
        // 计算需要多少个静音帧才能确认语音结束
        this.silentFramesRequired = silenceMillisThreshold / frameMillis;

        log.info("VAD Processor initialized. Energy Threshold: {}, Silence Threshold: {}ms ({} frames)",
                energyThreshold, silenceMillisThreshold, this.silentFramesRequired);
    }

    /**
     * 处理一个音频块以检测语音。
     * 此方法应使用固定大小的音频块连续调用。
     * 音频格式假定为16位有符号PCM，小端字节序。
     *
     * @param pcmAudioData 原始的16位PCM音频数据块。
     */
    public void process(byte[] pcmAudioData) {
        if (pcmAudioData == null || pcmAudioData.length == 0) {
            return;
        }

        double energy = calculateRmsEnergy(pcmAudioData);

        boolean isCurrentlySpeech = energy > energyThreshold;

        if (isCurrentlySpeech) {
            // 检测到语音
            if (!isSpeaking) {
                isSpeaking = true;
                log.debug("Speech started (Energy: {})", String.format("%.2f", energy));
                listener.onSpeechStart();
            }
            silentFrameCount = 0;
            listener.onSpeech(pcmAudioData);
        } else {
            // 检测到静音
            if (isSpeaking) {
                silentFrameCount++;
                if (silentFrameCount >= silentFramesRequired) {
                    isSpeaking = false;
                    log.debug("Speech ended (Silence frames: {})", silentFrameCount);
                    // 在触发 onSpeechEnd 之前，先将最后一个静音前的语音块发送出去
                    listener.onSpeechEnd();
                    silentFrameCount = 0; // 触发事件后重置
                } else {
                    // 仍在静音的宽限期内，将其视作持续语音的一部分（例如，单词之间的短暂暂停）
                    listener.onSpeech(pcmAudioData);
                }
            }
            // 如果不在说话状态且仍然是静音，则不执行任何操作。
        }
    }

    /**
     * 计算16位PCM音频块的均方根（RMS）能量。
     *
     * @param pcmAudioData 16位小端PCM音频数据。
     * @return 音频块的RMS能量。
     */
    private double calculateRmsEnergy(byte[] pcmAudioData) {
        long sumOfSquares = 0L;
        // 音频是16位的，所以每个采样点2个字节。
        int sampleCount = pcmAudioData.length / 2;

        // 使用ByteBuffer来正确处理字节序
        ByteBuffer buffer = ByteBuffer.wrap(pcmAudioData).order(ByteOrder.LITTLE_ENDIAN);

        for (int i = 0; i < sampleCount; i++) {
            // 读取2个字节组成一个short
            short sample = buffer.getShort();
            sumOfSquares += (long) sample * sample;
        }

        if (sampleCount == 0) {
            return 0.0;
        }

        double meanSquare = (double) sumOfSquares / sampleCount;
        return Math.sqrt(meanSquare);
    }
}