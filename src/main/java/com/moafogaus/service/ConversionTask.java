package com.moafogaus.service;

import com.moafogaus.model.MediaItem;
import javafx.application.Platform;
import javafx.concurrent.Task;
import ws.schild.jave.Encoder;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.EncodingAttributes;
import ws.schild.jave.info.MultimediaInfo;
import ws.schild.jave.progress.EncoderProgressListener;

import java.io.File;

public class ConversionTask extends Task<Void> implements EncoderProgressListener {

    private final MediaItem item;
    private final File outputDir;
    private long startTimeMs;

    public ConversionTask(MediaItem item, File outputDir) {
        this.item = item;
        this.outputDir = outputDir;
    }

    @Override
    protected Void call() throws Exception {
        File sourceFile = item.getSourceFile();

        // Verification of source file existence and permissions
        if (sourceFile == null || !sourceFile.exists() || !sourceFile.isFile() || !sourceFile.canRead()) {
            Platform.runLater(() -> {
                item.setStatus(MediaItem.Status.ERROR);
                item.setStatusMessage("Fichier source introuvable ou inaccessible");
                item.setProgress(0.0);
            });
            return null;
        }

        if (sourceFile.length() == 0) {
            Platform.runLater(() -> {
                item.setStatus(MediaItem.Status.ERROR);
                item.setStatusMessage("Fichier source vide (0 octet)");
                item.setProgress(0.0);
            });
            return null;
        }

        if (outputDir == null || (!outputDir.exists() && !outputDir.mkdirs())) {
            Platform.runLater(() -> {
                item.setStatus(MediaItem.Status.ERROR);
                item.setStatusMessage("Dossier de destination inaccessible");
                item.setProgress(0.0);
            });
            return null;
        }

        Platform.runLater(() -> {
            item.setStatus(MediaItem.Status.CONVERTING);
            item.setProgress(0.0);
            item.setStatusMessage("Démarrage...");
        });

        // Ensure total duration
        double totalDuration = item.getDurationSeconds();
        if (totalDuration <= 0) {
            totalDuration = FFmpegService.getInstance().extractDurationSeconds(item.getSourceFile());
            final double extractedDur = totalDuration;
            Platform.runLater(() -> item.setDurationSeconds(extractedDur));
        }

        File targetFile = FFmpegService.getInstance().determineOutputFile(item, outputDir);
        EncodingAttributes attrs = FFmpegService.getInstance().buildEncodingAttributes(item);

        MultimediaObject multimediaObject = new MultimediaObject(item.getSourceFile());
        Encoder encoder = new Encoder();

        startTimeMs = System.currentTimeMillis();

        try {
            encoder.encode(multimediaObject, targetFile, attrs, this);

            if (isCancelled()) {
                Platform.runLater(() -> {
                    item.setStatus(MediaItem.Status.CANCELLED);
                    item.setStatusMessage("Annulé par l'utilisateur");
                    item.setProgress(0.0);
                });
                return null;
            }

            Platform.runLater(() -> {
                item.setProgress(1.0);
                item.setStatus(MediaItem.Status.COMPLETED);
                item.setStatusMessage("Conversion terminée");
                item.setEta("00:00");
            });

        } catch (Throwable e) {
            e.printStackTrace();
            if (targetFile != null && targetFile.exists() && targetFile.length() == 0) {
                targetFile.delete();
            }
            if (isCancelled()) {
                Platform.runLater(() -> {
                    item.setStatus(MediaItem.Status.CANCELLED);
                    item.setStatusMessage("Annulé");
                });
            } else {
                String errorMsg = (e.getMessage() != null && !e.getMessage().trim().isEmpty())
                    ? e.getMessage()
                    : e.getClass().getSimpleName();
                Platform.runLater(() -> {
                    item.setStatus(MediaItem.Status.ERROR);
                    item.setStatusMessage("Erreur : " + errorMsg);
                });
            }
        }

        return null;
    }

    // JAVE2 EncoderProgressListener implementation
    @Override
    public void sourceInfo(MultimediaInfo info) {
        if (info != null && info.getDuration() > 0) {
            double durSec = info.getDuration() / 1000.0;
            Platform.runLater(() -> item.setDurationSeconds(durSec));
        }
    }

    @Override
    public void progress(int permil) {
        double progressVal = Math.min(1.0, Math.max(0.0, permil / 1000.0));

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

    @Override
    public void message(String message) {
        // Logs d'exécution
    }

    @Override
    protected void cancelled() {
        Platform.runLater(() -> {
            item.setStatus(MediaItem.Status.CANCELLED);
            item.setStatusMessage("Annulé");
        });
    }

    public MediaItem getItem() {
        return item;
    }
}
