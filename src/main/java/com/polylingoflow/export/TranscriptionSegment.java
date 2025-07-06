package com.polylingoflow.export;

/**
 * 代表一个转录的独立片段，包含文本及其开始和结束时间戳。
 * 这是一个不可变的数据类。
 *
 * @param startTimeMillis 片段在音频中的开始时间（毫秒）。
 * @param endTimeMillis   片段在音频中的结束时间（毫秒）。
 * @param text            该片段的转录文本。
 */
public record TranscriptionSegment(long startTimeMillis, long endTimeMillis, String text) {
}