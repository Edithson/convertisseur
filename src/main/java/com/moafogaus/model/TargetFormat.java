package com.moafogaus.model;

public enum TargetFormat {
    // Formats Audio
    MP3("MP3", "mp3", MediaType.AUDIO, "libmp3lame", "Audio MPEG Layer 3"),
    WAV("WAV", "wav", MediaType.AUDIO, "pcm_s16le", "Waveform Audio Uncompressed"),
    OGG("OGG Vorbis", "ogg", MediaType.AUDIO, "libvorbis", "Ogg Vorbis Audio"),
    OPUS("OPUS", "opus", MediaType.AUDIO, "libopus", "IETF Opus Audio Codec"),
    AAC("AAC", "aac", MediaType.AUDIO, "aac", "Advanced Audio Coding"),
    FLAC("FLAC", "flac", MediaType.AUDIO, "flac", "Free Lossless Audio Codec"),
    M4A("M4A", "m4a", MediaType.AUDIO, "aac", "MPEG-4 Audio"),

    // Formats Vidéo
    MP4("MP4 (H.264)", "mp4", MediaType.VIDEO, "libx264", "MPEG-4 Video H.264"),
    MKV("MKV (Matroska)", "mkv", MediaType.VIDEO, "libx264", "Matroska Video Container"),
    WEBM("WebM (VP9)", "webm", MediaType.VIDEO, "libvpx-vp9", "WebM Open Media Format"),
    AVI("AVI", "avi", MediaType.VIDEO, "mpeg4", "Audio Video Interleave"),
    MOV("QuickTime (MOV)", "mov", MediaType.VIDEO, "libx264", "Apple QuickTime Movie");

    private final String displayName;
    private final String extension;
    private final MediaType type;
    private final String defaultCodec;
    private final String description;

    TargetFormat(String displayName, String extension, MediaType type, String defaultCodec, String description) {
        this.displayName = displayName;
        this.extension = extension;
        this.type = type;
        this.defaultCodec = defaultCodec;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getExtension() {
        return extension;
    }

    public MediaType getType() {
        return type;
    }

    public String getDefaultCodec() {
        return defaultCodec;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
