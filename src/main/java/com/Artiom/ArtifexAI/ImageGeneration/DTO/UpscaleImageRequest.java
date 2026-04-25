package com.Artiom.ArtifexAI.ImageGeneration.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpscaleImageRequest {
    private Long projectId;
    private ImageInfo imageInfo;
    private UpscaleFactor upscaleFactor;
}
