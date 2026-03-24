package com.Artiom.ArtifexAI.ImageGeneration.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpriteSheetGenerationRequest {
    private String projectId;
    private String characterDescription;
    private String actionDescription;
    private List<ImageInfo> imageInfos;
}