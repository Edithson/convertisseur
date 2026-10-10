package com.moafogaus.service;

import com.moafogaus.model.ConversionSettings;
import com.moafogaus.model.MediaItem;
import com.moafogaus.model.MediaType;
import com.moafogaus.model.TargetFormat;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FFmpegService {

    private static FFmpegService instance;

    private String ffmpegPath = "ffmpeg";
    private String ffprobePath = "ffprobe";
    private Boolean ffmpegAvailable = null;

    private FFmpegService() {}

    public static synchronized FFmpegService getInstance() {
        if (instance == null) {
            instance = new FFmpegService();
        }
        return instance;
    }

    public boolean isFFmpegAvailable() {
        if (ffmpegAvailable != null) {
            return ffmpegAvailable;
        }
        try {
            Process process = new ProcessBuilder(ffmpegPath, "-version").start();
            int exitCode = process.waitFor();
            ffmpegAvailable = (exitCode == 0);
        } catch (Exception e) {
            ffmpegAvailable = false;
        }
        return ffmpegAvailable;
    }

    public double extractDurationSeconds(File mediaFile) {
        if (mediaFile == null || !mediaFile.exists()) return 0;

        // Try ffprobe first
        try {
            List<String> cmd = List.of(
                ffprobePath,
                "-v", "error",
                "-show_entries", "format=duration",
                "-of", "default=noprint_wrappers=1:nokey=1",
                mediaFile.getAbsolutePath()
            );
            Process process = new ProcessBuilder(cmd).start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line = reader.readLine();
                if (line != null && !line.trim().isEmpty()) {
                    return Double.parseDouble(line.trim());
                }
            }
        } catch (Exception ignored) {}

        // Fallback to parsing ffmpeg -i output
        try {
            List<String> cmd = List.of(ffmpegPath, "-i", mediaFile.getAbsolutePath());
            Process process = new ProcessBuilder(cmd).start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                Pattern pattern = Pattern.compile("Duration:\\s*(\\d+):(\\d+):(\\d+(?:\\.\\d+)?)");
                while ((line = reader.readLine()) != null) {
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        int hours = Integer.parseInt(matcher.group(1));
                        int minutes = Integer.parseInt(matcher.group(2));
                        double seconds = Double.parseDouble(matcher.group(3));
                        return hours * 3600 + minutes * 60 + seconds;
                    }
                }
            }
        } catch (Exception ignored) {}

        return 0;
    }

    public List<String> buildFFmpegCommand(MediaItem item, File outputDir) {
        List<String> cmd = new ArrayList<>();
        cmd.add(ffmpegPath);
        cmd.add("-y"); // Overwrite output files without asking

        ConversionSettings settings = item.getSettings();

        // Trim start time if defined
        if (settings.getStartTime() != null && settings.getStartTime() > 0) {
            cmd.add("-ss");
            cmd.add(String.format(java.util.Locale.US, "%.2f", settings.getStartTime()));
        }

        // Input file
        cmd.add("-i");
        cmd.add(item.getSourceFile().getAbsolutePath());

        // Trim end/duration if defined
        if (settings.getEndTime() != null && settings.getEndTime() > 0) {
            cmd.add("-to");
            cmd.add(String.format(java.util.Locale.US, "%.2f", settings.getEndTime()));
        }

        TargetFormat format = item.getTargetFormat();

        if (format.getType() == MediaType.AUDIO) {
            // Audio extraction / audio conversion
            cmd.add("-vn"); // Disable video stream
            cmd.add("-c:a");
            cmd.add(format.getDefaultCodec());

            if (settings.getAudioBitrate() != null && !settings.getAudioBitrate().equalsIgnoreCase("Auto")) {
                cmd.add("-b:a");
                cmd.add(settings.getAudioBitrate());
            }

            if (settings.getAudioSampleRate() != null && !settings.getAudioSampleRate().equalsIgnoreCase("Auto")) {
                cmd.add("-ar");
                cmd.add(settings.getAudioSampleRate());
            }

            if (settings.getAudioChannels() > 0) {
                cmd.add("-ac");
                cmd.add(String.valueOf(settings.getAudioChannels()));
            }

            if (settings.isNormalizeAudio()) {
                cmd.add("-af");
                cmd.add("loudnorm");
            }
        } else {
            // Video conversion
            cmd.add("-c:v");
            cmd.add(format.getDefaultCodec());

            if (settings.getVideoResolution() != null && !settings.getVideoResolution().equalsIgnoreCase("Source")) {
                String scaleArg = null;
                String res = settings.getVideoResolution();
                if ("1080p".equals(res)) {
                    scaleArg = "scale=1920:1080:force_original_aspect_ratio=decrease";
                } else if ("720p".equals(res)) {
                    scaleArg = "scale=1280:720:force_original_aspect_ratio=decrease";
                } else if ("480p".equals(res)) {
                    scaleArg = "scale=854:480:force_original_aspect_ratio=decrease";
                }
                if (scaleArg != null) {
                    cmd.add("-vf");
                    cmd.add(scaleArg);
                }
            }

            // Audio codec for video container
            cmd.add("-c:a");
            if (format == TargetFormat.WEBM) {
                cmd.add("libopus");
            } else {
                cmd.add("aac");
            }
            if (settings.getAudioBitrate() != null && !settings.getAudioBitrate().equalsIgnoreCase("Auto")) {
                cmd.add("-b:a");
                cmd.add(settings.getAudioBitrate());
            }
        }

        // Output File
        String originalBaseName = getBaseName(item.getSourceFile().getName());
        String outFileName = originalBaseName + "." + format.getExtension();
        File outputFile = new File(outputDir, outFileName);

        // If output file equals input file, append "_converted"
        if (outputFile.getAbsolutePath().equals(item.getSourceFile().getAbsolutePath())) {
            outFileName = originalBaseName + "_converted." + format.getExtension();
            outputFile = new File(outputDir, outFileName);
        }

        item.setOutputPath(outputFile.getAbsolutePath());
        cmd.add(outputFile.getAbsolutePath());

        return cmd;
    }

    private String getBaseName(String filename) {
        int dot = filename.lastIndexOf('.');
        return (dot > 0) ? filename.substring(0, dot) : filename;
    }
}
