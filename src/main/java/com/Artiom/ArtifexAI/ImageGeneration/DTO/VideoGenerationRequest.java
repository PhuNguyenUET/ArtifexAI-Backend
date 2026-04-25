package com.Artiom.ArtifexAI.ImageGeneration.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoGenerationRequest {
    private Long projectId;
    private ImageInfo referenceImage;
    private String prompt;
    private VideoLengthType videoLength;
}
