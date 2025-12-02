package com.Artiom.ArtifexAI.Media.Controller;

import com.Artiom.ArtifexAI.Common.Api.ApiResponse;
import com.Artiom.ArtifexAI.Media.DTO.ImageClientUploadDTO;
import com.Artiom.ArtifexAI.Media.DTO.MediaDTO;
import com.Artiom.ArtifexAI.Media.Service.MediaService;
import io.jsonwebtoken.lang.Assert;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "media_management")
@RequestMapping("/api/media/v1")
public class MediaController {
    @Value("${api.token}")
    private String apiToken;

    private final MediaService mediaService;

    @PostMapping("/upload_client")
    public ResponseEntity<ApiResponse> uploadClientImage(@RequestHeader("X-auth-token") String token, @RequestBody ImageClientUploadDTO imageDTO) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            MediaDTO clientImage = mediaService.addClientImage(imageDTO);
            return ResponseEntity.ok(ApiResponse.success("Upload client image successfully", clientImage));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse> deleteImage(@RequestHeader("X-auth-token") String token,
                                                   @RequestParam String imageId) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            mediaService.deleteMedia(imageId);
            return ResponseEntity.ok(ApiResponse.success("Delete image successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @GetMapping("/get_by_id")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(implementation = MediaDTO.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> getImageById(@RequestHeader("X-auth-token") String token,
                                                    @RequestParam String imageId) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            return ResponseEntity.ok(ApiResponse.success("Image fetched successfully", mediaService.getMediaById(imageId)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @GetMapping("/gallery")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = {@Content(array = @ArraySchema(schema = @Schema(implementation = MediaDTO.class)), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> getGallery(@RequestHeader("X-auth-token") String token) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            return ResponseEntity.ok(ApiResponse.success("Gallery fetched successfully", mediaService.getGallery()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @GetMapping("/get_by_album")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = {@Content(array = @ArraySchema(schema = @Schema(implementation = MediaDTO.class)), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> getAlbum(@RequestHeader("X-auth-token") String token,
                                                @RequestParam String albumId) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            return ResponseEntity.ok(ApiResponse.success("Album images fetched successfully", mediaService.getMediasByAlbum(albumId)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }
}
