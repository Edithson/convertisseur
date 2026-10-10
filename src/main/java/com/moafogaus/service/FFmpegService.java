package com.moafogaus.service;

import com.moafogaus.model.ConversionSettings;
import com.moafogaus.model.MediaItem;
import com.moafogaus.model.MediaType;
import com.moafogaus.model.TargetFormat;
import ws.schild.jave.Encoder;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;
import ws.schild.jave.encode.VideoAttributes;
import ws.schild.jave.info.MultimediaInfo;
import ws.schild.jave.info.VideoSize;
import ws.schild.jave.process.ffmpeg.DefaultFFMPEGLocator;

import java.io.File;

public class FFmpegService {

    private static FFmpegService instance;
    private Boolean javeAvailable = null;

    private FFmpegService() {}

    public static synchronized FFmpegService getInstance() {
        if (instance == null) {
            instance = new FFmpegService();
        }
        return instance;
    }

    public boolean isFFmpegAvailable() {
        if (javeAvailable != null) {
            return javeAvailable;
        }
        try {
            DefaultFFMPEGLocator locator = new DefaultFFMPEGLocator();
            String path = locator.getExecutablePath();
            File f = new File(path);
            javeAvailable = (f.exists() && f.canExecute()) || isSystemFFmpegAvailable();
        } catch (Throwable e) {
            javeAvailable = isSystemFFmpegAvailable();
        }
        return javeAvailable;
    }

    private boolean isSystemFFmpegAvailable() {
        try {
            Process p = new ProcessBuilder("ffmpeg", "-version").start();
            return p.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    public double extractDurationSeconds(File mediaFile) {
        if (mediaFile == null || !mediaFile.exists()) return 0;

        try {
            MultimediaObject multimediaObject = new MultimediaObject(mediaFile);
            MultimediaInfo info = multimediaObject.getInfo();
            if (info != null && info.getDuration() > 0) {
                return info.getDuration() / 1000.0;
            }
        } catch (Exception ignored) {}

        return 0;
    }

    public EncodingAttributes buildEncodingAttributes(MediaItem item) {
        EncodingAttributes attrs = new EncodingAttributes();
        TargetFormat format = item.getTargetFormat();

        attrs.setOutputFormat(format.getExtension());

        ConversionSettings settings = item.getSettings();

        // Trim start & duration
        if (settings.getStartTime() != null && settings.getStartTime() > 0) {
            attrs.setOffset(settings.getStartTime().floatValue());
        }
        if (settings.getEndTime() != null && settings.getEndTime() > 0) {
            float start = (settings.getStartTime() != null) ? settings.getStartTime().floatValue() : 0f;
            float duration = (float) (settings.getEndTime() - start);
            if (duration > 0) {
                attrs.setDuration(duration);
            }
        }

        // Audio Attributes
        AudioAttributes audio = new AudioAttributes();
        
        if (format.getType() == MediaType.AUDIO) {
            String codec = switchAudioCodec(format.getDefaultCodec());
            audio.setCodec(codec);
        } else {
            // Video format audio stream
            if (format == TargetFormat.WEBM) {
                audio.setCodec("opus");
            } else {
                audio.setCodec("aac");
            }
        }

        if (settings.getAudioBitrate() != null && !settings.getAudioBitrate().equalsIgnoreCase("Auto")) {
            try {
                String bStr = settings.getAudioBitrate().toLowerCase().replace("k", "");
                int bitrateBps = Integer.parseInt(bStr) * 1000;
                audio.setBitRate(bitrateBps);
            } catch (Exception ignored) {}
        }

        if (settings.getAudioSampleRate() != null && !settings.getAudioSampleRate().equalsIgnoreCase("Auto")) {
            try {
                int sr = Integer.parseInt(settings.getAudioSampleRate());
                audio.setSamplingRate(sr);
            } catch (Exception ignored) {}
        }

        if (settings.getAudioChannels() > 0) {
            audio.setChannels(settings.getAudioChannels());
        }

        attrs.setAudioAttributes(audio);

        // Video Attributes (if format is VIDEO)
        if (format.getType() == MediaType.VIDEO) {
            VideoAttributes video = new VideoAttributes();
            String vCodec = switchVideoCodec(format.getDefaultCodec());

            video.setCodec(vCodec);

            if (settings.getVideoResolution() != null && !settings.getVideoResolution().equalsIgnoreCase("Source")) {
                String res = settings.getVideoResolution();
                if ("1080p".equals(res)) {
                    video.setSize(new VideoSize(1920, 1080));
                } else if ("720p".equals(res)) {
                    video.setSize(new VideoSize(1280, 720));
                } else if ("480p".equals(res)) {
                    video.setSize(new VideoSize(854, 480));
                }
            }
            attrs.setVideoAttributes(video);
        }

        return attrs;
    }

    private String switchAudioCodec(String rawCodec) {
        if ("libmp3lame".equalsIgnoreCase(rawCodec)) return "mp3";
        if ("libvorbis".equalsIgnoreCase(rawCodec)) return "vorbis";
        if ("libopus".equalsIgnoreCase(rawCodec)) return "opus";
        if ("pcm_s16le".equalsIgnoreCase(rawCodec)) return "pcm_s16le";
        return "aac";
    }

    private String switchVideoCodec(String rawCodec) {
        if ("libx264".equalsIgnoreCase(rawCodec)) return "h264";
        if ("libvpx-vp9".equalsIgnoreCase(rawCodec)) return "vp9";
        if ("mpeg4".equalsIgnoreCase(rawCodec)) return "mpeg4";
        return "h264";
    }

    public File determineOutputFile(MediaItem item, File outputDir) {
        String originalBaseName = getBaseName(item.getSourceFile().getName());
        String outFileName = originalBaseName + "." + item.getTargetFormat().getExtension();
        File outputFile = new File(outputDir, outFileName);

        if (outputFile.getAbsolutePath().equals(item.getSourceFile().getAbsolutePath())) {
            outFileName = originalBaseName + "_converted." + item.getTargetFormat().getExtension();
            outputFile = new File(outputDir, outFileName);
        }

        item.setOutputPath(outputFile.getAbsolutePath());
        return outputFile;
    }

    private String getBaseName(String filename) {
        int dot = filename.lastIndexOf('.');
        return (dot > 0) ? filename.substring(0, dot) : filename;
    }
}
