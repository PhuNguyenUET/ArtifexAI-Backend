package com.Artiom.ArtifexAI.Image.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PresignedImageInfo {
    private String imagePresignedUrl = "";
    private long presignedUrlExpireTime = 0;
}