package com.Artiom.ArtifexAI.Media.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class PresignedMediaInfo {
    @Column(columnDefinition = "TEXT")
    private String mediaPresignedUrl = "";
    private long presignedUrlExpireTime = 0;
}