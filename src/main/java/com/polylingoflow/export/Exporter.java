package com.polylingoflow.export;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Handles exporting transcription and translation results to various file formats.
 */
public class Exporter {

    private static final Logger log = LoggerFactory.getLogger(Exporter.class);

    public void exportAsTxt(String content, Path destination) throws IOException {
        Files.writeString(destination, content);
        log.info("Successfully exported content to {}", destination);
    }

    public void exportAsSrt(Object srtData, Path destination) {
        // TODO: Implement SRT (SubRip) file format export.
        // This will require timestamps for each transcribed segment.
        log.info("Exporting to SRT format is not yet implemented.");
    }

    public void exportAsVtt(Object vttData, Path destination) {
        // TODO: Implement VTT (WebVTT) file format export.
        log.info("Exporting to VTT format is not yet implemented.");
    }
}