package com.moafogaus.controller;

import com.moafogaus.model.MediaItem;
import com.moafogaus.model.TargetFormat;
import com.moafogaus.service.ConversionTask;
import com.moafogaus.service.FFmpegService;
import com.moafogaus.service.SettingsService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainController {

    @FXML private Label ffmpegStatusBadge;
    @FXML private Label outputFolderLabel;
    @FXML private ComboBox<TargetFormat> globalFormatCombo;

    @FXML private VBox dropZone;
    @FXML private ScrollPane queueScrollPane;
    @FXML private VBox queueContainer;

    @FXML private Label queueSummaryLabel;
    @FXML private Button clearListBtn;
    @FXML private Button cancelAllBtn;
    @FXML private Button convertAllBtn;

    private final ObservableList<MediaItem> mediaItems = FXCollections.observableArrayList();
    private final List<ConversionTask> activeTasks = new ArrayList<>();
    private ExecutorService executorService;

    @FXML
    public void initialize() {
        // FFmpeg Check
        boolean ffmpegReady = FFmpegService.getInstance().isFFmpegAvailable();
        if (ffmpegReady) {
            ffmpegStatusBadge.setText("● FFmpeg Prêt");
            ffmpegStatusBadge.getStyleClass().setAll("badge-status");
        } else {
            ffmpegStatusBadge.setText("● FFmpeg Non Détecté");
            ffmpegStatusBadge.getStyleClass().setAll("badge-status", "badge-status-error");
        }

        // Output Dir setup
        File outputDir = SettingsService.getInstance().getOutputDir();
        outputFolderLabel.setText(outputDir.getAbsolutePath());

        // Global format combo setup
        globalFormatCombo.getItems().setAll(TargetFormat.values());
        globalFormatCombo.setValue(TargetFormat.MP3);

        // List change listener
        mediaItems.addListener((ListChangeListener<MediaItem>) c -> {
            updateQueueVisibility();
            updateSummary();
        });

        updateQueueVisibility();
    }

    private void updateQueueVisibility() {
        boolean hasItems = !mediaItems.isEmpty();
        dropZone.setVisible(!hasItems);
        dropZone.setManaged(!hasItems);
        queueScrollPane.setVisible(hasItems);
        queueScrollPane.setManaged(hasItems);
    }

    private void updateSummary() {
        int count = mediaItems.size();
        long totalBytes = mediaItems.stream().mapToLong(MediaItem::getFileSizeBytes).sum();
        if (count == 0) {
            queueSummaryLabel.setText("0 fichier dans la file");
        } else {
            queueSummaryLabel.setText(count + " fichier" + (count > 1 ? "s" : "") + " (" + MediaItem.formatFileSize(totalBytes) + ")");
        }
    }

    @FXML
    private void handleAddFiles() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner des fichiers audio ou vidéo");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Tous les fichiers multimédia", "*.mp4", "*.mkv", "*.avi", "*.webm", "*.mov", "*.mp3", "*.wav", "*.ogg", "*.opus", "*.aac", "*.flac", "*.m4a", "*.wma"),
            new FileChooser.ExtensionFilter("Fichiers Vidéo", "*.mp4", "*.mkv", "*.avi", "*.webm", "*.mov"),
            new FileChooser.ExtensionFilter("Fichiers Audio", "*.mp3", "*.wav", "*.ogg", "*.opus", "*.aac", "*.flac", "*.m4a")
        );

        Stage stage = (Stage) rootStackPaneGetStage();
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(stage);

        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            addFilesToQueue(selectedFiles);
        }
    }

    private Stage rootStackPaneGetStage() {
        return (Stage) dropZone.getScene().getWindow();
    }

    @FXML
    private void handleDragOver(DragEvent event) {
        if (event.getDragboard().hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY);
            if (!dropZone.getStyleClass().contains("drop-zone-drag-over")) {
                dropZone.getStyleClass().add("drop-zone-drag-over");
            }
        }
        event.consume();
    }

    @FXML
    private void handleDragDropped(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean success = false;
        dropZone.getStyleClass().remove("drop-zone-drag-over");

        if (db.hasFiles()) {
            addFilesToQueue(db.getFiles());
            success = true;
        }
        event.setDropCompleted(success);
        event.consume();
    }

    private void addFilesToQueue(List<File> files) {
        TargetFormat defaultTarget = globalFormatCombo.getValue() != null ? globalFormatCombo.getValue() : TargetFormat.MP3;

        for (File file : files) {
            if (file.isFile()) {
                MediaItem item = new MediaItem(file);
                item.setTargetFormat(defaultTarget);
                mediaItems.add(item);
                createItemRow(item);

                // Fetch duration in background
                new Thread(() -> {
                    double dur = FFmpegService.getInstance().extractDurationSeconds(file);
                    Platform.runLater(() -> item.setDurationSeconds(dur));
                }).start();
            }
        }
    }

    private void createItemRow(MediaItem item) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/moafogaus/media-item-row.fxml"));
            Parent rowNode = loader.load();

            MediaItemRowController controller = loader.getController();
            controller.setItem(
                item,
                this::handleRemoveItem,
                this::handleOpenItemSettings
            );

            queueContainer.getChildren().add(rowNode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleRemoveItem(MediaItem item) {
        int index = mediaItems.indexOf(item);
        if (index >= 0) {
            mediaItems.remove(index);
            if (index < queueContainer.getChildren().size()) {
                queueContainer.getChildren().remove(index);
            }
        }
    }

    private void handleOpenItemSettings(MediaItem item) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/moafogaus/settings-view.fxml"));
            Parent dialogNode = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Réglages - " + item.getName());
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(rootStackPaneGetStage());

            Scene scene = new Scene(dialogNode);
            dialogStage.setScene(scene);

            SettingsDialogController controller = loader.getController();
            controller.setItem(item, dialogStage);

            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleApplyGlobalFormat() {
        TargetFormat selected = globalFormatCombo.getValue();
        if (selected != null) {
            for (MediaItem item : mediaItems) {
                item.setTargetFormat(selected);
            }
        }
    }

    @FXML
    private void handleConvertAll() {
        if (!FFmpegService.getInstance().isFFmpegAvailable()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("FFmpeg non trouvé");
            alert.setHeaderText("FFmpeg est requis pour la conversion.");
            alert.setContentText("Veuillez installer ffmpeg sur votre système (ex: sudo apt install ffmpeg) puis relancez l'application.");
            alert.showAndWait();
            return;
        }

        if (mediaItems.isEmpty()) return;

        File outputDir = SettingsService.getInstance().getOutputDir();
        int maxTasks = SettingsService.getInstance().getConcurrentTasks();
        executorService = Executors.newFixedThreadPool(maxTasks);
        activeTasks.clear();

        cancelAllBtn.setVisible(true);
        cancelAllBtn.setManaged(true);
        convertAllBtn.setDisable(true);

        for (MediaItem item : mediaItems) {
            if (item.getStatus() != MediaItem.Status.COMPLETED) {
                ConversionTask task = new ConversionTask(item, outputDir);
                activeTasks.add(task);
                executorService.submit(task);
            }
        }

        // Shutdown executor service after tasks complete
        new Thread(() -> {
            executorService.shutdown();
            while (!executorService.isTerminated()) {
                try { Thread.sleep(300); } catch (InterruptedException ignored) {}
            }
            Platform.runLater(() -> {
                cancelAllBtn.setVisible(false);
                cancelAllBtn.setManaged(false);
                convertAllBtn.setDisable(false);
            });
        }).start();
    }

    @FXML
    private void handleCancelAll() {
        for (ConversionTask task : activeTasks) {
            task.cancel();
        }
        if (executorService != null) {
            executorService.shutdownNow();
        }
        cancelAllBtn.setVisible(false);
        cancelAllBtn.setManaged(false);
        convertAllBtn.setDisable(false);
    }

    @FXML
    private void handleClearList() {
        handleCancelAll();
        mediaItems.clear();
        queueContainer.getChildren().clear();
    }

    @FXML
    private void handleChooseOutputFolder() {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Choisir le dossier de destination");
        File current = SettingsService.getInstance().getOutputDir();
        if (current.exists()) dc.setInitialDirectory(current);

        File selected = dc.showDialog(rootStackPaneGetStage());
        if (selected != null) {
            SettingsService.getInstance().setOutputDir(selected);
            outputFolderLabel.setText(selected.getAbsolutePath());
        }
    }

    @FXML
    private void handleOpenOutputFolder() {
        File folder = SettingsService.getInstance().getOutputDir();
        if (folder.exists() && Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().open(folder);
            } catch (IOException ignored) {}
        }
    }
}
