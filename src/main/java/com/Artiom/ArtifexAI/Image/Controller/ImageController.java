package com.Artiom.ArtifexAI.Image.Controller;

import com.Artiom.ArtifexAI.Common.Api.ApiResponse;
import com.Artiom.ArtifexAI.Image.DTO.ImageDTO;
import com.Artiom.ArtifexAI.Image.Service.ImageService;
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
@Tag(name = "image_management")
@RequestMapping("/api/image/v1")
public class ImageController {
    @Value("${api.token}")
    private String apiToken;

    private final ImageService imageService;

    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse> deleteImage(@RequestHeader("X-auth-token") String token,
                                                   @RequestParam String imageId) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            imageService.deleteImage(imageId);
            return ResponseEntity.ok(ApiResponse.success("Delete image successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @GetMapping("/get_by_id")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(implementation = ImageDTO.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> getImageById(@RequestHeader("X-auth-token") String token,
                                                    @RequestParam String imageId) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            return ResponseEntity.ok(ApiResponse.success("Image fetched successfully", imageService.getImageById(imageId)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @GetMapping("/gallery")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = {@Content(array = @ArraySchema(schema = @Schema(implementation = ImageDTO.class)), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> getGallery(@RequestHeader("X-auth-token") String token) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            return ResponseEntity.ok(ApiResponse.success("Gallery fetched successfully", imageService.getGallery()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @GetMapping("/get_by_album")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = {@Content(array = @ArraySchema(schema = @Schema(implementation = ImageDTO.class)), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> getAlbum(@RequestHeader("X-auth-token") String token,
                                                @RequestParam String albumId) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            return ResponseEntity.ok(ApiResponse.success("Album images fetched successfully", imageService.getImagesByAlbum(albumId)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }
}
