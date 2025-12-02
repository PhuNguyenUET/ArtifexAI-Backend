package com.Artiom.ArtifexAI.ImageGeneration.Service;

import com.Artiom.ArtifexAI.ImageGeneration.DTO.VideoGenerationRequest;
import com.Artiom.ArtifexAI.ImageGeneration.DTO.VideoGenerationResponse;

public interface VideoGenerationService {
    VideoGenerationResponse generateVideo(VideoGenerationRequest request);
}
