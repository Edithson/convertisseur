package com.moafogaus.model;

public enum MediaType {
    AUDIO("Audio"),
    VIDEO("Vidéo");

    private final String label;

    MediaType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
