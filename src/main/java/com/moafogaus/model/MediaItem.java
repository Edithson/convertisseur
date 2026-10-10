package com.moafogaus.model;

import javafx.beans.property.*;
import java.io.File;

public class MediaItem {

    public enum Status {
        PENDING("En attente"),
        CONVERTING("En conversion"),
        COMPLETED("Terminé"),
        ERROR("Erreur"),
        CANCELLED("Annulé");

        private final String label;
        Status(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    private final File sourceFile;
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty inputExtension = new SimpleStringProperty();
    private final LongProperty fileSizeBytes = new SimpleLongProperty();
    private final StringProperty formattedSize = new SimpleStringProperty();
    private final DoubleProperty durationSeconds = new SimpleDoubleProperty(0);
    private final StringProperty formattedDuration = new SimpleStringProperty("--:--");
    
    private final ObjectProperty<TargetFormat> targetFormat = new SimpleObjectProperty<>();
    private final ObjectProperty<Status> status = new SimpleObjectProperty<>(Status.PENDING);
    private final DoubleProperty progress = new SimpleDoubleProperty(0.0);
    private final StringProperty statusMessage = new SimpleStringProperty("Prêt");
    private final StringProperty outputPath = new SimpleStringProperty();
    private final StringProperty eta = new SimpleStringProperty("--:--");

    private final ConversionSettings settings;

    public MediaItem(File sourceFile) {
        this.sourceFile = sourceFile;
        this.name.set(sourceFile.getName());
        this.fileSizeBytes.set(sourceFile.length());
        this.formattedSize.set(formatFileSize(sourceFile.length()));

        String fileName = sourceFile.getName();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            this.inputExtension.set(fileName.substring(dotIndex + 1).toLowerCase());
        } else {
            this.inputExtension.set("???");
        }

        // Target format default MP3
        this.targetFormat.set(TargetFormat.MP3);
        this.settings = new ConversionSettings();
    }

    public static String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char pre = "KMGTPE".charAt(exp - 1);
        return String.format("%.1f %cB", bytes / Math.pow(1024, exp), pre);
    }

    public static String formatDuration(double seconds) {
        if (seconds <= 0) return "--:--";
        long sec = (long) seconds;
        long hrs = sec / 3600;
        long mins = (sec % 3600) / 60;
        long secs = sec % 60;
        if (hrs > 0) {
            return String.format("%02d:%02d:%02d", hrs, mins, secs);
        } else {
            return String.format("%02d:%02d", mins, secs);
        }
    }

    // Getters and Properties
    public File getSourceFile() { return sourceFile; }

    public String getName() { return name.get(); }
    public StringProperty nameProperty() { return name; }

    public String getInputExtension() { return inputExtension.get(); }
    public StringProperty inputExtensionProperty() { return inputExtension; }

    public long getFileSizeBytes() { return fileSizeBytes.get(); }
    public LongProperty fileSizeBytesProperty() { return fileSizeBytes; }

    public String getFormattedSize() { return formattedSize.get(); }
    public StringProperty formattedSizeProperty() { return formattedSize; }

    public double getDurationSeconds() { return durationSeconds.get(); }
    public DoubleProperty durationSecondsProperty() { return durationSeconds; }
    public void setDurationSeconds(double duration) {
        this.durationSeconds.set(duration);
        this.formattedDuration.set(formatDuration(duration));
    }
    public String getFormattedDuration() { return formattedDuration.get(); }
    public StringProperty formattedDurationProperty() { return formattedDuration; }

    public TargetFormat getTargetFormat() { return targetFormat.get(); }
    public void setTargetFormat(TargetFormat format) { this.targetFormat.set(format); }
    public ObjectProperty<TargetFormat> targetFormatProperty() { return targetFormat; }

    public Status getStatus() { return status.get(); }
    public void setStatus(Status status) { this.status.set(status); }
    public ObjectProperty<Status> statusProperty() { return status; }

    public double getProgress() { return progress.get(); }
    public void setProgress(double progress) { this.progress.set(progress); }
    public DoubleProperty progressProperty() { return progress; }

    public String getStatusMessage() { return statusMessage.get(); }
    public void setStatusMessage(String message) { this.statusMessage.set(message); }
    public StringProperty statusMessageProperty() { return statusMessage; }

    public String getOutputPath() { return outputPath.get(); }
    public void setOutputPath(String path) { this.outputPath.set(path); }
    public StringProperty outputPathProperty() { return outputPath; }

    public String getEta() { return eta.get(); }
    public void setEta(String eta) { this.eta.set(eta); }
    public StringProperty etaProperty() { return eta; }

    public ConversionSettings getSettings() { return settings; }
}
