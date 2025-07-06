package com.polylingoflow.export;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 处理将转录和翻译结果导出为多种文件格式。
 */
public class Exporter {

    private static final Logger log = LoggerFactory.getLogger(Exporter.class);

    /**
     * 将纯文本内容导出为 .txt 文件。
     *
     * @param content     要写入的字符串内容。
     * @param destination 目标文件的路径。
     * @throws IOException 如果写入文件时发生 I/O 错误。
     */
    public void exportAsTxt(String content, Path destination) throws IOException {
        Files.writeString(destination, content);
        log.info("成功将内容导出到 {}", destination);
    }

    /**
     * 将转录片段列表导出为 SRT (SubRip) 字幕格式。
     *
     * @param segments    一个 TranscriptionSegment 对象的列表，每个对象都包含开始/结束时间和文本。
     * @param destination 目标 .srt 文件的路径。
     * @throws IOException 如果写入文件时发生 I/O 错误。
     */
    public void exportAsSrt(List<TranscriptionSegment> segments, Path destination) throws IOException {
        if (segments == null || segments.isEmpty()) {
            log.warn("片段列表为空或为null。无法导出为 SRT。");
            return;
        }

        StringBuilder srtContent = new StringBuilder();
        for (int i = 0; i < segments.size(); i++) {
            TranscriptionSegment segment = segments.get(i);
            srtContent.append(i + 1) // 字幕序号
                    .append(System.lineSeparator())
                    .append(formatTimestamp(segment.startTimeMillis(), ","))
                    .append(" --> ")
                    .append(formatTimestamp(segment.endTimeMillis(), ","))
                    .append(System.lineSeparator())
                    .append(segment.text())
                    .append(System.lineSeparator())
                    .append(System.lineSeparator());
        }

        Files.writeString(destination, srtContent.toString());
        log.info("成功将 {} 个片段导出到 SRT 文件: {}", segments.size(), destination);
    }

    /**
     * 将转录片段列表导出为 VTT (WebVTT) 字幕格式。
     *
     * @param segments    一个 TranscriptionSegment 对象的列表，每个对象都包含开始/结束时间和文本。
     * @param destination 目标 .vtt 文件的路径。
     * @throws IOException 如果写入文件时发生 I/O 错误。
     */
    public void exportAsVtt(List<TranscriptionSegment> segments, Path destination) throws IOException {
        if (segments == null || segments.isEmpty()) {
            log.warn("片段列表为空或为null。无法导出为 VTT。");
            return;
        }

        StringBuilder vttContent = new StringBuilder("WEBVTT");
        vttContent.append(System.lineSeparator()).append(System.lineSeparator());

        for (TranscriptionSegment segment : segments) {
            vttContent.append(formatTimestamp(segment.startTimeMillis(), "."))
                    .append(" --> ")
                    .append(formatTimestamp(segment.endTimeMillis(), "."))
                    .append(System.lineSeparator())
                    .append(segment.text())
                    .append(System.lineSeparator())
                    .append(System.lineSeparator());
        }

        Files.writeString(destination, vttContent.toString());
        log.info("成功将 {} 个片段导出到 VTT 文件: {}", segments.size(), destination);
    }

    /**
     * 将毫秒格式化为 HH:MM:SS,ms 或 HH:MM:SS.ms 格式。
     *
     * @param millis    毫秒时间。
     * @param separator 毫秒部分之前使用的分隔符（SRT为","，VTT为"."）。
     * @return 格式化的时间戳字符串。
     */
    private String formatTimestamp(long millis, String separator) {
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;
        long ms = millis % 1000;

        return String.format("%02d:%02d:%02d%s%03d", hours, minutes, seconds, separator, ms);
    }
}