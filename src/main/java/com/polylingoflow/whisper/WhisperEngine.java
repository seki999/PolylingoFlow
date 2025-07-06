package com.polylingoflow.whisper;

import io.github.ggerganov.whisper_jni.WhisperJNI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 一个围绕 WhisperJNI 库的封装器，用于处理语音到文本的转录。
 * 此类管理 WhisperJNI 实例的生命周期，包括加载本地库、初始化模型和执行转录。
 * 它实现了 AutoCloseable 接口以进行正确的资源管理。
 */
public class WhisperEngine implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(WhisperEngine.class);
    private WhisperJNI whisper;
    private boolean isInitialized = false;

    // 静态代码块，在类加载时仅加载一次本地库。
    // 这对于 JNI 封装器的正常工作至关重要。
    static {
        try {
            log.info("正在尝试加载 WhisperJNI 本地库...");
            WhisperJNI.loadLibrary();
            log.info("WhisperJNI 本地库加载成功。");
        } catch (IOException e) {
            log.error("致命错误：加载 WhisperJNI 本地库失败。请确保本地文件 (.dll, .so, .dylib) 可访问。", e);
            // 这是一个严重故障，因此我们抛出一个运行时异常来中止初始化。
            throw new RuntimeException("无法加载 WhisperJNI 本地库", e);
        }
    }

    public WhisperEngine() {
        // 实际的模型初始化通过 initialize() 方法完成。
    }

    /**
     * 使用指定的模型初始化 Whisper 引擎。
     * 在进行任何转录之前，必须调用此方法。
     *
     * @param modelPath Whisper 模型文件的路径（例如，ggml-base.en.bin）。
     */
    public void initialize(Path modelPath) {
        if (isInitialized) {
            log.warn("Whisper 引擎已初始化。忽略此次调用。");
            return;
        }
        if (modelPath == null || !Files.exists(modelPath)) {
            log.error("模型路径为 null 或文件不存在: {}", modelPath);
            throw new IllegalArgumentException("提供了无效的模型路径。");
        }

        try {
            log.info("正在使用模型初始化 Whisper 上下文: {}", modelPath);
            whisper = new WhisperJNI();
            // 如果需要，你可以在此处自定义参数，例如，whisper.init(modelPath, true) 以使用GPU。
            whisper.init(modelPath.toString());
            isInitialized = true;
            log.info("Whisper 引擎初始化成功。");
        } catch (Exception e) {
            log.error("使用模型初始化 Whisper 引擎失败: {}", modelPath, e);
            // 确保在初始化中途失败时进行清理。
            close();
        }
    }

    /**
     * 将原始的16位PCM音频字节数组转录为文本。
     *
     * @param pcm16leAudioData 原始音频数据（16位、有符号、小端PCM）。
     * @return 转录后的文本，如果转录失败则返回错误消息。
     */
    public String transcribe(byte[] pcm16leAudioData) {
        if (!isInitialized) {
            log.error("Whisper 引擎未初始化。请先调用 initialize()。");
            return "[错误: 引擎未初始化]";
        }
        if (pcm16leAudioData == null || pcm16leAudioData.length == 0) {
            log.warn("调用转录时使用了空的音频数据。");
            return "";
        }

        try {
            // 根据 Whisper 的要求，将16位PCM字节数组转换为32位浮点数组。
            float[] floatAudioData = convertPcm16leToFloat32(pcm16leAudioData);

            // 运行转录。
            return whisper.full(floatAudioData);
        } catch (Exception e) {
            log.error("转录过程中发生错误。", e);
            return "[错误: 转录失败]";
        }
    }

    /**
     * 将16位小端PCM字节数组转换为32位浮点数组。
     * 浮点值被归一化到 [-1.0, 1.0] 的范围。
     *
     * @param pcmData 输入的字节数组。
     * @return 转换后的浮点数组。
     */
    private float[] convertPcm16leToFloat32(byte[] pcmData) {
        // 每个16位采样点占2个字节。
        int numSamples = pcmData.length / 2;
        float[] floatData = new float[numSamples];
        // 使用 ByteBuffer 来正确处理字节序（小端序是PCM的常见格式）。
        ByteBuffer buffer = ByteBuffer.wrap(pcmData).order(ByteOrder.LITTLE_ENDIAN);

        for (int i = 0; i < numSamples; i++) {
            // 读取2个字节组成一个short，然后归一化为float。
            // 除以 32768.0f 将 short 值（范围 -32768 到 32767）归一化到 float 范围 [-1.0, 1.0]。
            floatData[i] = buffer.getShort() / 32768.0f;
        }
        return floatData;
    }

    /**
     * 释放 Whisper 引擎持有的本地资源。
     * 在完成引擎使用后，调用此方法至关重要。
     */
    @Override
    public void close() {
        if (whisper != null) {
            whisper.free();
            whisper = null;
            isInitialized = false;
            log.info("Whisper 引擎资源已释放。");
        }
    }
}