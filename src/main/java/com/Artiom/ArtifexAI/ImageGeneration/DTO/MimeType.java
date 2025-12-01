package com.Artiom.ArtifexAI.ImageGeneration.DTO;

import lombok.Getter;

@Getter
public enum MimeType {
    JPEG("image/jpeg"),
    PNG("image/png");

    private final String value;

    MimeType(String value) {
        this.value = value;
    }
}