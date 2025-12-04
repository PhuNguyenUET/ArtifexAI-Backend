package com.Artiom.ArtifexAI.ImageGeneration.Controller;

import com.Artiom.ArtifexAI.Common.Api.ApiResponse;
import com.Artiom.ArtifexAI.ImageGeneration.DTO.*;
import com.Artiom.ArtifexAI.ImageGeneration.Service.ImageGenerationService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "image_generation")
@RequestMapping("/api/image_generation/v1")
public class ImageGenerationController {
    @Value("${api.token}")
    private String apiToken;

    private final ImageGenerationService imageGenerationService;

    @PostMapping("/splash_art")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = ImageGenerationResponse.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> generateImageFromPrompt(@RequestHeader("X-auth-token") String token,
                                        @RequestBody SplashArtGenerationRequest request) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            return ResponseEntity.ok(ApiResponse.success("Image generation successful", imageGenerationService.generateSplashArt(request)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PostMapping("/variation")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = ImageGenerationResponse.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> generateImageVariation(@RequestHeader("X-auth-token") String token,
                                        @RequestBody ImageVariationRequest request) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            return ResponseEntity.ok(ApiResponse.success("Image variation generation successful", imageGenerationService.generateImageVariation(request)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PostMapping("/sprite_sheet")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = ImageGenerationResponse.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> generateSpriteSheet(@RequestHeader("X-auth-token") String token,
                                        @RequestBody SpriteSheetGenerationRequest request) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            return ResponseEntity.ok(ApiResponse.success("Sprite sheet generation successful", imageGenerationService.generateSpriteSheet(request)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PostMapping("/style_change")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = ImageGenerationResponse.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> changeImageStyle(@RequestHeader("X-auth-token") String token,
                                        @RequestBody ImageStyleChangeRequest request) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            return ResponseEntity.ok(ApiResponse.success("Image style change successful", imageGenerationService.changeImageStyle(request)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PostMapping("/masked_edit")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = ImageGenerationResponse.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> editImageWithImageMask(@RequestHeader("X-auth-token") String token,
                                        @RequestBody ImageEditWithMaskRequest request) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            return ResponseEntity.ok(ApiResponse.success("Image edit with mask successful", imageGenerationService.editImageWithImageMask(request)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PostMapping("/upscale")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = ImageGenerationResponse.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> upsaleImage(@RequestHeader("X-auth-token") String token,
                                        @RequestBody UpscaleImageRequest request) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            return ResponseEntity.ok(ApiResponse.success("Image upscaling successful", imageGenerationService.upsaleImage(request)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }
}
