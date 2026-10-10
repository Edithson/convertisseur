package com.moafogaus.controller;

import com.moafogaus.model.ConversionSettings;
import com.moafogaus.model.MediaItem;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class SettingsDialogController {

    @FXML private Label fileNameLabel;
    @FXML private ComboBox<String> bitrateCombo;
    @FXML private ComboBox<String> sampleRateCombo;
    @FXML private ComboBox<String> channelsCombo;
    @FXML private ComboBox<String> resolutionCombo;
    @FXML private CheckBox normalizeCheckBox;
    @FXML private TextField startTimeField;
    @FXML private TextField endTimeField;

    private MediaItem item;
    private Stage stage;

    public void setItem(MediaItem item, Stage stage) {
        this.item = item;
        this.stage = stage;

        fileNameLabel.setText(item.getName());

        bitrateCombo.getItems().setAll("Auto", "320k", "256k", "192k", "128k", "96k", "64k");
        sampleRateCombo.getItems().setAll("Auto", "44100", "48000", "22050");
        channelsCombo.getItems().setAll("Source (Inchangé)", "Stéréo (2 canaux)", "Mono (1 canal)");
        resolutionCombo.getItems().setAll("Source", "1080p", "720p", "480p");

        ConversionSettings settings = item.getSettings();

        bitrateCombo.setValue(settings.getAudioBitrate() != null ? settings.getAudioBitrate() : "192k");
        sampleRateCombo.setValue(settings.getAudioSampleRate() != null ? settings.getAudioSampleRate() : "44100");

        if (settings.getAudioChannels() == 1) {
            channelsCombo.setValue("Mono (1 canal)");
        } else if (settings.getAudioChannels() == 2) {
            channelsCombo.setValue("Stéréo (2 canaux)");
        } else {
            channelsCombo.setValue("Source (Inchangé)");
        }

        resolutionCombo.setValue(settings.getVideoResolution() != null ? settings.getVideoResolution() : "Source");
        normalizeCheckBox.setSelected(settings.isNormalizeAudio());

        if (settings.getStartTime() != null) {
            startTimeField.setText(String.valueOf(settings.getStartTime()));
        }
        if (settings.getEndTime() != null) {
            endTimeField.setText(String.valueOf(settings.getEndTime()));
        }
    }

    @FXML
    private void handleSave() {
        if (item != null) {
            ConversionSettings settings = item.getSettings();

            settings.setAudioBitrate(bitrateCombo.getValue());
            settings.setAudioSampleRate(sampleRateCombo.getValue());

            String ch = channelsCombo.getValue();
            if ("Mono (1 canal)".equals(ch)) {
                settings.setAudioChannels(1);
            } else if ("Stéréo (2 canaux)".equals(ch)) {
                settings.setAudioChannels(2);
            } else {
                settings.setAudioChannels(0);
            }

            settings.setVideoResolution(resolutionCombo.getValue());
            settings.setNormalizeAudio(normalizeCheckBox.isSelected());

            try {
                String startTxt = startTimeField.getText().trim();
                if (!startTxt.isEmpty()) {
                    settings.setStartTime(Double.parseDouble(startTxt));
                } else {
                    settings.setStartTime(null);
                }
            } catch (NumberFormatException ignored) {}

            try {
                String endTxt = endTimeField.getText().trim();
                if (!endTxt.isEmpty()) {
                    settings.setEndTime(Double.parseDouble(endTxt));
                } else {
                    settings.setEndTime(null);
                }
            } catch (NumberFormatException ignored) {}
        }
        if (stage != null) {
            stage.close();
        }
    }

    @FXML
    private void handleCancel() {
        if (stage != null) {
            stage.close();
        }
    }
}
