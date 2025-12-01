package com.Artiom.ArtifexAI.ImageGeneration.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SplashArtGenerationRequest {
    private String projectId;
    private String splashDescription;
    private int numberOfOutputs;
}
