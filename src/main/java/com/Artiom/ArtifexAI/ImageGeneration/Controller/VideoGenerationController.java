package com.Artiom.ArtifexAI.ImageGeneration.Controller;

import com.Artiom.ArtifexAI.Common.Api.ApiResponse;
import com.Artiom.ArtifexAI.ImageGeneration.DTO.VideoGenerationRequest;
import com.Artiom.ArtifexAI.ImageGeneration.DTO.VideoGenerationResponse;
import com.Artiom.ArtifexAI.ImageGeneration.Service.VideoGenerationService;
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
@Tag(name = "video_generation")
@RequestMapping("/api/video_generation/v1")
public class VideoGenerationController {
    @Value("${api.token}")
    private String apiToken;

    private final VideoGenerationService videoGenerationService;

    @PostMapping("/generate")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = VideoGenerationResponse.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> generateVideo(@RequestHeader("X-auth-token") String token,
                                                       @RequestBody VideoGenerationRequest request) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            return ResponseEntity.ok(ApiResponse.success("Video generation successful", videoGenerationService.generateVideo(request)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }
}

