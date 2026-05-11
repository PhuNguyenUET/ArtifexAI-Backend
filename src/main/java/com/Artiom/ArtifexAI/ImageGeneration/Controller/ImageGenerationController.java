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
            ImageGenerationResponse result = imageGenerationService.editImageWithImageMask(request);
            return ResponseEntity.ok(ApiResponse.success("Image edit with mask successful", result));
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

    @PostMapping("/flux2/splash_art")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = ImageGenerationResponse.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> generateSplashArtFlux2(@RequestHeader("X-auth-token") String token,
                                        @RequestBody SplashArtGenerationRequest request) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            return ResponseEntity.ok(ApiResponse.success("Flux-2 splash art generation successful", imageGenerationService.generateSplashArtFlux2(request)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PostMapping("/flux2/variation")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = ImageGenerationResponse.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> generateImageVariationFlux2(@RequestHeader("X-auth-token") String token,
                                        @RequestBody ImageVariationRequest request) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            return ResponseEntity.ok(ApiResponse.success("Flux-2 image variation successful", imageGenerationService.generateImageVariationFlux2(request)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PostMapping("/flux2/sprite_sheet")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = ImageGenerationResponse.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> generateSpriteSheetFlux2(@RequestHeader("X-auth-token") String token,
                                        @RequestBody SpriteSheetGenerationRequest request) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            return ResponseEntity.ok(ApiResponse.success("Flux-2 sprite sheet generation successful", imageGenerationService.generateSpriteSheetFlux2(request)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PostMapping("/flux2/style_change")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = ImageGenerationResponse.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> changeImageStyleFlux2(@RequestHeader("X-auth-token") String token,
                                        @RequestBody ImageStyleChangeRequest request) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            return ResponseEntity.ok(ApiResponse.success("Flux-2 image style change successful", imageGenerationService.changeImageStyleFlux2(request)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    // ── Qwen endpoints ────────────────────────────────────────────────────

    @PostMapping("/qwen/splash_art")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = ImageGenerationResponse.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> generateSplashArtQwen(@RequestHeader("X-auth-token") String token,
                                        @RequestBody SplashArtGenerationRequest request) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            return ResponseEntity.ok(ApiResponse.success("Qwen splash art generation successful", imageGenerationService.generateSplashArtQwen(request)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PostMapping("/qwen/variation")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = ImageGenerationResponse.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> generateImageVariationQwen(@RequestHeader("X-auth-token") String token,
                                        @RequestBody ImageVariationRequest request) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            return ResponseEntity.ok(ApiResponse.success("Qwen image variation successful", imageGenerationService.generateImageVariationQwen(request)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PostMapping("/qwen/sprite_sheet")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = ImageGenerationResponse.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> generateSpriteSheetQwen(@RequestHeader("X-auth-token") String token,
                                        @RequestBody SpriteSheetGenerationRequest request) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            return ResponseEntity.ok(ApiResponse.success("Qwen sprite sheet generation successful", imageGenerationService.generateSpriteSheetQwen(request)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PostMapping("/qwen/style_change")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = ImageGenerationResponse.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> changeImageStyleQwen(@RequestHeader("X-auth-token") String token,
                                        @RequestBody ImageStyleChangeRequest request) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            return ResponseEntity.ok(ApiResponse.success("Qwen image style change successful", imageGenerationService.changeImageStyleQwen(request)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }


    // ── GPT-Image-2 endpoints ─────────────────────────────────────────────

    @PostMapping("/gpt/splash_art")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = ImageGenerationResponse.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> generateSplashArtGPT(@RequestHeader("X-auth-token") String token,
                                        @RequestBody SplashArtGenerationRequest request) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            return ResponseEntity.ok(ApiResponse.success("GPT-Image-2 splash art generation successful", imageGenerationService.generateSplashArtGPT(request)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PostMapping("/gpt/variation")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = ImageGenerationResponse.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> generateImageVariationGPT(@RequestHeader("X-auth-token") String token,
                                        @RequestBody ImageVariationRequest request) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            return ResponseEntity.ok(ApiResponse.success("GPT-Image-2 image variation successful", imageGenerationService.generateImageVariationGPT(request)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PostMapping("/gpt/sprite_sheet")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = ImageGenerationResponse.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> generateSpriteSheetGPT(@RequestHeader("X-auth-token") String token,
                                        @RequestBody SpriteSheetGenerationRequest request) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            return ResponseEntity.ok(ApiResponse.success("GPT-Image-2 sprite sheet generation successful", imageGenerationService.generateSpriteSheetGPT(request)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PostMapping("/gpt/style_change")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = ImageGenerationResponse.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> changeImageStyleGPT(@RequestHeader("X-auth-token") String token,
                                        @RequestBody ImageStyleChangeRequest request) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            return ResponseEntity.ok(ApiResponse.success("GPT-Image-2 image style change successful", imageGenerationService.changeImageStyleGPT(request)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }
}
