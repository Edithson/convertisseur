package com.moafogaus.service;

import com.moafogaus.model.MediaItem;
import javafx.application.Platform;
import javafx.concurrent.Task;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConversionTask extends Task<Void> {

    private final MediaItem item;
    private final File outputDir;
    private Process process;

    public ConversionTask(MediaItem item, File outputDir) {
        this.item = item;
        this.outputDir = outputDir;
    }

    @Override
    protected Void call() throws Exception {
        Platform.runLater(() -> {
            item.setStatus(MediaItem.Status.CONVERTING);
            item.setProgress(0.0);
            item.setStatusMessage("Démarrage...");
        });

        // First, ensure total duration is available
        double totalDuration = item.getDurationSeconds();
        if (totalDuration <= 0) {
            totalDuration = FFmpegService.getInstance().extractDurationSeconds(item.getSourceFile());
            final double extractedDur = totalDuration;
            Platform.runLater(() -> item.setDurationSeconds(extractedDur));
        }

        List<String> command = FFmpegService.getInstance().buildFFmpegCommand(item, outputDir);

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true); // Merge stderr into stdout for easy line-by-line reading

        process = pb.start();

        long startTimeMs = System.currentTimeMillis();
        Pattern timePattern = Pattern.compile("time=\\s*(\\d+):(\\d+):(\\d+(?:\\.\\d+)?)");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (isCancelled()) {
                    process.destroyForcibly();
                    Platform.runLater(() -> {
                        item.setStatus(MediaItem.Status.CANCELLED);
                        item.setStatusMessage("Annulé par l'utilisateur");
                        item.setProgress(0.0);
                    });
                    return null;
                }

                Matcher matcher = timePattern.matcher(line);
                if (matcher.find() && totalDuration > 0) {
                    int hrs = Integer.parseInt(matcher.group(1));
                    int mins = Integer.parseInt(matcher.group(2));
                    double secs = Double.parseDouble(matcher.group(3));
                    double currentSeconds = hrs * 3600 + mins * 60 + secs;

                    double progressVal = Math.min(1.0, Math.max(0.0, currentSeconds / totalDuration));
                    
                    // Estimate ETA
                    long elapsedMs = System.currentTimeMillis() - startTimeMs;
                    long estimatedTotalMs = (progressVal > 0) ? (long) (elapsedMs / progressVal) : 0;
                    long remainingMs = Math.max(0, estimatedTotalMs - elapsedMs);
                    long remainingSecs = remainingMs / 1000;

                    String etaStr = MediaItem.formatDuration(remainingSecs);

                    Platform.runLater(() -> {
                        item.setProgress(progressVal);
                        item.setEta(etaStr);
                        item.setStatusMessage(String.format("%.0f%%", progressVal * 100));
                    });
                }
            }
        }

        int exitCode = process.waitFor();

        if (isCancelled()) {
            Platform.runLater(() -> {
                item.setStatus(MediaItem.Status.CANCELLED);
                item.setStatusMessage("Annulé");
                item.setProgress(0.0);
            });
            return null;
        }

        if (exitCode == 0) {
            Platform.runLater(() -> {
                item.setProgress(1.0);
                item.setStatus(MediaItem.Status.COMPLETED);
                item.setStatusMessage("Conversion terminée");
                item.setEta("00:00");
            });
        } else {
            Platform.runLater(() -> {
                item.setStatus(MediaItem.Status.ERROR);
                item.setStatusMessage("Erreur FFmpeg (code " + exitCode + ")");
            });
        }

        return null;
    }

    @Override
    protected void cancelled() {
        if (process != null && process.isAlive()) {
            process.destroyForcibly();
        }
        Platform.runLater(() -> {
            item.setStatus(MediaItem.Status.CANCELLED);
            item.setStatusMessage("Annulé");
        });
    }

    public MediaItem getItem() {
        return item;
    }
}
