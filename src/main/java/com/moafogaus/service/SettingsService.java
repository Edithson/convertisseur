package com.moafogaus.service;

import java.io.File;
import java.util.prefs.Preferences;

public class SettingsService {

    private static final String KEY_OUTPUT_DIR = "output_dir";
    private static final String KEY_DEFAULT_FORMAT = "default_format";
    private static final String KEY_CONCURRENT_TASKS = "concurrent_tasks";

    private final Preferences prefs = Preferences.userNodeForPackage(SettingsService.class);

    private static SettingsService instance;

    private SettingsService() {}

    public static synchronized SettingsService getInstance() {
        if (instance == null) {
            instance = new SettingsService();
        }
        return instance;
    }

    public File getOutputDir() {
        String defaultPath = System.getProperty("user.home") + File.separator + "ConvertedMedia";
        String path = prefs.get(KEY_OUTPUT_DIR, defaultPath);
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public void setOutputDir(File dir) {
        if (dir != null && dir.exists()) {
            prefs.put(KEY_OUTPUT_DIR, dir.getAbsolutePath());
        }
    }

    public String getDefaultFormat() {
        return prefs.get(KEY_DEFAULT_FORMAT, "MP3");
    }

    public void setDefaultFormat(String format) {
        prefs.put(KEY_DEFAULT_FORMAT, format);
    }

    public int getConcurrentTasks() {
        return prefs.getInt(KEY_CONCURRENT_TASKS, 2);
    }

    public void setConcurrentTasks(int tasks) {
        prefs.putInt(KEY_CONCURRENT_TASKS, Math.max(1, Math.min(8, tasks)));
    }
}
