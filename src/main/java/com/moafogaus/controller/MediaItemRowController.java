package com.moafogaus.controller;

import com.moafogaus.model.MediaItem;
import com.moafogaus.model.TargetFormat;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

public class MediaItemRowController {

    @FXML private Label inputFormatBadge;
    @FXML private Label sizeLabel;
    @FXML private Label titleLabel;
    @FXML private Label durationLabel;
    @FXML private Label statusBadgeLabel;
    @FXML private Label statusMessageLabel;
    @FXML private Label etaLabel;
    @FXML private ProgressBar progressBar;
    @FXML private ComboBox<TargetFormat> targetFormatCombo;
    @FXML private Button settingsBtn;
    @FXML private Button openOutputBtn;
    @FXML private Button removeBtn;

    private MediaItem item;
    private Consumer<MediaItem> onRemoveHandler;
    private Consumer<MediaItem> onSettingsHandler;

    public void setItem(MediaItem item, Consumer<MediaItem> onRemoveHandler, Consumer<MediaItem> onSettingsHandler) {
        this.item = item;
        this.onRemoveHandler = onRemoveHandler;
        this.onSettingsHandler = onSettingsHandler;

        // Bind text & properties
        inputFormatBadge.setText(item.getInputExtension().toUpperCase());
        sizeLabel.setText(item.getFormattedSize());
        titleLabel.setText(item.getName());
        durationLabel.setText("• " + item.getFormattedDuration());

        statusMessageLabel.textProperty().bind(item.statusMessageProperty());
        etaLabel.textProperty().bind(item.etaProperty());
        progressBar.progressProperty().bind(item.progressProperty());

        // Target Formats combo setup
        targetFormatCombo.getItems().setAll(TargetFormat.values());
        targetFormatCombo.setValue(item.getTargetFormat());

        targetFormatCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                item.setTargetFormat(newVal);
            }
        });

        // Status listener for completed open output button visibility and badge styling
        item.statusProperty().addListener((obs, oldVal, newVal) -> updateStatusBadge(newVal));
        updateStatusBadge(item.getStatus());
    }

    private void updateStatusBadge(MediaItem.Status status) {
        if (status == null) return;

        statusBadgeLabel.setText(status.getLabel());
        statusBadgeLabel.getStyleClass().removeAll("badge-status", "badge-status-warning", "badge-status-error");

        switch (status) {
            case CONVERTING:
            case COMPLETED:
                statusBadgeLabel.getStyleClass().add("badge-status");
                break;
            case PENDING:
            case CANCELLED:
                statusBadgeLabel.getStyleClass().add("badge-status-warning");
                break;
            case ERROR:
                statusBadgeLabel.getStyleClass().add("badge-status-error");
                break;
        }

        boolean isCompleted = (status == MediaItem.Status.COMPLETED);
        openOutputBtn.setVisible(isCompleted);
        openOutputBtn.setManaged(isCompleted);
    }

    @FXML
    private void handleRemove() {
        if (onRemoveHandler != null && item != null) {
            onRemoveHandler.accept(item);
        }
    }

    @FXML
    private void handleOpenSettings() {
        if (onSettingsHandler != null && item != null) {
            onSettingsHandler.accept(item);
        }
    }

    @FXML
    private void handleOpenOutput() {
        if (item != null && item.getOutputPath() != null) {
            File file = new File(item.getOutputPath());
            if (file.exists()) {
                try {
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().open(file.getParentFile());
                    }
                } catch (IOException ignored) {}
            }
        }
    }
}
