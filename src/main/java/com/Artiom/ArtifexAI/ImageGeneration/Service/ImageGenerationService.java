package com.Artiom.ArtifexAI.ImageGeneration.Service;

import com.Artiom.ArtifexAI.ImageGeneration.DTO.*;

public interface ImageGenerationService {
    // ── Gemini (existing) ──────────────────────────────────────────────────
    ImageGenerationResponse generateSplashArt(SplashArtGenerationRequest request);

    ImageGenerationResponse generateImageVariation(ImageVariationRequest request);

    ImageGenerationResponse generateSpriteSheet(SpriteSheetGenerationRequest request);

    ImageGenerationResponse changeImageStyle(ImageStyleChangeRequest request);

    ImageGenerationResponse editImageWithImageMask(ImageEditWithMaskRequest request);

    ImageGenerationResponse upsaleImage(UpscaleImageRequest request);

    // ── Flux-2 / HuggingFace ──────────────────────────────────────────────
    ImageGenerationResponse generateSplashArtFlux2(SplashArtGenerationRequest request);

    ImageGenerationResponse generateImageVariationFlux2(ImageVariationRequest request);

    ImageGenerationResponse generateSpriteSheetFlux2(SpriteSheetGenerationRequest request);

    ImageGenerationResponse changeImageStyleFlux2(ImageStyleChangeRequest request);

    // ── Qwen / HuggingFace ────────────────────────────────────────────────
    ImageGenerationResponse generateSplashArtQwen(SplashArtGenerationRequest request);

    ImageGenerationResponse generateImageVariationQwen(ImageVariationRequest request);

    ImageGenerationResponse generateSpriteSheetQwen(SpriteSheetGenerationRequest request);

    ImageGenerationResponse changeImageStyleQwen(ImageStyleChangeRequest request);
}
