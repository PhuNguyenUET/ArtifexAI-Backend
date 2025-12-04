package com.Artiom.ArtifexAI.ImageGeneration.Service;

import com.Artiom.ArtifexAI.ImageGeneration.DTO.*;

public interface ImageGenerationService {
    ImageGenerationResponse generateSplashArt(SplashArtGenerationRequest request);

    ImageGenerationResponse generateImageVariation(ImageVariationRequest request);

    ImageGenerationResponse generateSpriteSheet(SpriteSheetGenerationRequest request);

    ImageGenerationResponse changeImageStyle(ImageStyleChangeRequest request);

    ImageGenerationResponse editImageWithImageMask(ImageEditWithMaskRequest request);

    ImageGenerationResponse upsaleImage(UpscaleImageRequest request);
}
