package com.moafogaus.model;

public class ConversionSettings {
    private String audioBitrate = "192k";
    private String audioSampleRate = "44100";
    private int audioChannels = 0; // 0 = keep source, 1 = mono, 2 = stereo
    private String videoResolution = "Source"; // "Source", "1080p", "720p", "480p"
    private Double startTime = null; // null = start from 0
    private Double endTime = null;   // null = end at duration
    private boolean normalizeAudio = false;

    public ConversionSettings() {}

    public ConversionSettings(ConversionSettings other) {
        if (other != null) {
            this.audioBitrate = other.audioBitrate;
            this.audioSampleRate = other.audioSampleRate;
            this.audioChannels = other.audioChannels;
            this.videoResolution = other.videoResolution;
            this.startTime = other.startTime;
            this.endTime = other.endTime;
            this.normalizeAudio = other.normalizeAudio;
        }
    }

    public String getAudioBitrate() { return audioBitrate; }
    public void setAudioBitrate(String audioBitrate) { this.audioBitrate = audioBitrate; }

    public String getAudioSampleRate() { return audioSampleRate; }
    public void setAudioSampleRate(String audioSampleRate) { this.audioSampleRate = audioSampleRate; }

    public int getAudioChannels() { return audioChannels; }
    public void setAudioChannels(int audioChannels) { this.audioChannels = audioChannels; }

    public String getVideoResolution() { return videoResolution; }
    public void setVideoResolution(String videoResolution) { this.videoResolution = videoResolution; }

    public Double getStartTime() { return startTime; }
    public void setStartTime(Double startTime) { this.startTime = startTime; }

    public Double getEndTime() { return endTime; }
    public void setEndTime(Double endTime) { this.endTime = endTime; }

    public boolean isNormalizeAudio() { return normalizeAudio; }
    public void setNormalizeAudio(boolean normalizeAudio) { this.normalizeAudio = normalizeAudio; }
}
