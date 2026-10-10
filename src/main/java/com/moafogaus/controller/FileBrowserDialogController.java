package com.moafogaus.controller;

import com.moafogaus.model.MediaItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;

public class FileBrowserDialogController {

    @FXML private TextField addressBarField;
    @FXML private CheckBox selectAllCheckBox;
    @FXML private Label fileCountLabel;
    @FXML private ListView<FileItemEntry> fileListView;
    @FXML private Button importSelectedBtn;

    private File currentDirectory;
    private Stage stage;
    private Consumer<List<File>> onFilesSelectedHandler;

    private static final Set<String> MEDIA_EXTENSIONS = Set.of(
        "mp4", "mkv", "avi", "webm", "mov",
        "mp3", "wav", "ogg", "opus", "aac", "flac", "m4a", "wma", "m4v"
    );

    public static class FileItemEntry {
        private final File file;
        private final boolean isDirectory;
        private boolean selected;

        public FileItemEntry(File file, boolean isDirectory) {
            this.file = file;
            this.isDirectory = isDirectory;
            this.selected = !isDirectory; // Select files by default
        }

        public File getFile() { return file; }
        public boolean isDirectory() { return isDirectory; }
        public boolean isSelected() { return selected; }
        public void setSelected(boolean selected) { this.selected = selected; }
    }

    public void init(File initialDir, Stage stage, Consumer<List<File>> onFilesSelectedHandler) {
        this.stage = stage;
        this.onFilesSelectedHandler = onFilesSelectedHandler;

        if (initialDir == null || !initialDir.exists()) {
            initialDir = new File(System.getProperty("user.home"));
        }
        navigateTo(initialDir);

        fileListView.setCellFactory(param -> new ListCell<>() {
            private final CheckBox checkBox = new CheckBox();
            private final Label iconLabel = new Label();
            private final Label nameLabel = new Label();
            private final Label sizeLabel = new Label();
            private final HBox layout = new HBox(12);

            {
                nameLabel.getStyleClass().add("item-title");
                nameLabel.setStyle("-fx-font-size: 13px;");
                sizeLabel.getStyleClass().add("item-meta");
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                layout.setAlignment(Pos.CENTER_LEFT);
                layout.getChildren().addAll(checkBox, iconLabel, nameLabel, spacer, sizeLabel);

                checkBox.setOnAction(e -> {
                    FileItemEntry entry = getItem();
                    if (entry != null && !entry.isDirectory()) {
                        entry.setSelected(checkBox.isSelected());
                        updateImportCount();
                    }
                });
            }

            @Override
            protected void updateItem(FileItemEntry entry, boolean empty) {
                super.updateItem(entry, empty);
                if (empty || entry == null) {
                    setGraphic(null);
                } else {
                    nameLabel.setText(entry.getFile().getName());
                    if (entry.isDirectory()) {
                        checkBox.setVisible(false);
                        checkBox.setManaged(false);
                        iconLabel.setText("📁 ");
                        sizeLabel.setText("Dossier");
                    } else {
                        checkBox.setVisible(true);
                        checkBox.setManaged(true);
                        checkBox.setSelected(entry.isSelected());
                        
                        String ext = getExtension(entry.getFile());
                        if (isAudio(ext)) {
                            iconLabel.setText("🎵 ");
                        } else {
                            iconLabel.setText("🎬 ");
                        }
                        sizeLabel.setText(MediaItem.formatFileSize(entry.getFile().length()));
                    }
                    setGraphic(layout);
                }
            }
        });

        // Double click to navigate directory
        fileListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                FileItemEntry selected = fileListView.getSelectionModel().getSelectedItem();
                if (selected != null && selected.isDirectory()) {
                    navigateTo(selected.getFile());
                }
            }
        });
    }

    private void navigateTo(File dir) {
        if (dir == null || !dir.isDirectory()) return;
        this.currentDirectory = dir;
        this.addressBarField.setText(dir.getAbsolutePath());

        File[] files = dir.listFiles();
        ObservableList<FileItemEntry> entries = FXCollections.observableArrayList();

        if (files != null) {
            // Directories first, then media files
            List<File> dirs = new ArrayList<>();
            List<File> mediaFiles = new ArrayList<>();

            for (File f : files) {
                if (f.isHidden()) continue;
                if (f.isDirectory()) {
                    dirs.add(f);
                } else {
                    String ext = getExtension(f);
                    if (MEDIA_EXTENSIONS.contains(ext.toLowerCase())) {
                        mediaFiles.add(f);
                    }
                }
            }

            dirs.sort(Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER));
            mediaFiles.sort(Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER));

            for (File d : dirs) {
                entries.add(new FileItemEntry(d, true));
            }
            for (File m : mediaFiles) {
                entries.add(new FileItemEntry(m, false));
            }
        }

        fileListView.setItems(entries);
        selectAllCheckBox.setSelected(true);
        updateImportCount();
    }

    private void updateImportCount() {
        int mediaCount = 0;
        int selectedCount = 0;

        for (FileItemEntry entry : fileListView.getItems()) {
            if (!entry.isDirectory()) {
                mediaCount++;
                if (entry.isSelected()) selectedCount++;
            }
        }

        fileCountLabel.setText(mediaCount + " fichier(s) multimédia dans ce dossier");
        importSelectedBtn.setText("⚡ Importer les fichiers sélectionnés (" + selectedCount + ")");
        importSelectedBtn.setDisable(selectedCount == 0);
    }

    @FXML
    private void handleAddressBarNavigate() {
        String path = addressBarField.getText().trim();
        File target = new File(path);
        if (target.exists() && target.isDirectory()) {
            navigateTo(target);
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Dossier introuvable");
            alert.setHeaderText(null);
            alert.setContentText("Le chemin spécifié n'existe pas ou n'est pas un dossier valide.");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleGoUp() {
        if (currentDirectory != null && currentDirectory.getParentFile() != null) {
            navigateTo(currentDirectory.getParentFile());
        }
    }

    @FXML
    private void handleQuickHome() {
        navigateTo(new File(System.getProperty("user.home")));
    }

    @FXML
    private void handleQuickMusic() {
        File home = new File(System.getProperty("user.home"));
        File music = new File(home, "Musique");
        if (!music.exists()) music = new File(home, "Music");
        if (music.exists()) navigateTo(music);
        else navigateTo(home);
    }

    @FXML
    private void handleQuickVideos() {
        File home = new File(System.getProperty("user.home"));
        File vids = new File(home, "Vidéos");
        if (!vids.exists()) vids = new File(home, "Videos");
        if (vids.exists()) navigateTo(vids);
        else navigateTo(home);
    }

    @FXML
    private void handleQuickDownloads() {
        File home = new File(System.getProperty("user.home"));
        File dl = new File(home, "Téléchargements");
        if (!dl.exists()) dl = new File(home, "Downloads");
        if (dl.exists()) navigateTo(dl);
        else navigateTo(home);
    }

    @FXML
    private void handleSelectAllToggle() {
        boolean selected = selectAllCheckBox.isSelected();
        for (FileItemEntry entry : fileListView.getItems()) {
            if (!entry.isDirectory()) {
                entry.setSelected(selected);
            }
        }
        fileListView.refresh();
        updateImportCount();
    }

    @FXML
    private void handleImportSelected() {
        List<File> selectedFiles = new ArrayList<>();
        for (FileItemEntry entry : fileListView.getItems()) {
            if (!entry.isDirectory() && entry.isSelected()) {
                selectedFiles.add(entry.getFile());
            }
        }

        if (!selectedFiles.isEmpty() && onFilesSelectedHandler != null) {
            onFilesSelectedHandler.accept(selectedFiles);
        }

        if (stage != null) stage.close();
    }

    @FXML
    private void handleClose() {
        if (stage != null) stage.close();
    }

    private static String getExtension(File f) {
        String name = f.getName();
        int dot = name.lastIndexOf('.');
        return (dot > 0 && dot < name.length() - 1) ? name.substring(dot + 1) : "";
    }

    private static boolean isAudio(String ext) {
        return Set.of("mp3", "wav", "ogg", "opus", "flac", "m4a", "aac", "wma").contains(ext.toLowerCase());
    }
}
