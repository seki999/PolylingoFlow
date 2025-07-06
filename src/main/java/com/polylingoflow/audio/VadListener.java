package com.polylingoflow.audio;

/**
 * 用于从VadProcessor接收事件的侦听器接口。
 * 通过此接口，调用者可以对完整的语音片段进行操作。
 */
public interface VadListener {
    /**
     * 当在一段静音后首次检测到语音时调用。
     */
    void onSpeechStart();

    /**
     * 对于属于语音片段的每个音频块，都会调用此方法。
     * @param audioData PCM音频数据块。
     */
    void onSpeech(byte[] audioData);

    /**
     * 当一个语音片段结束，随后是一段静音时调用。
     * 在这里，你可以获得完整的语音数据并将其发送到识别引擎。
     */
    void onSpeechEnd();
}