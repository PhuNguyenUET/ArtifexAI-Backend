package com.Artiom.ArtifexAI.Media.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PresignedMediaInfo {
    private String mediaPresignedUrl = "";
    private long presignedUrlExpireTime = 0;
}