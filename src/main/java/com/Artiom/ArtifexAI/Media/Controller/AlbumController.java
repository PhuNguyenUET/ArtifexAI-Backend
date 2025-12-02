package com.Artiom.ArtifexAI.Media.Controller;

import com.Artiom.ArtifexAI.Common.Api.ApiResponse;
import com.Artiom.ArtifexAI.Media.DTO.AlbumCreateDTO;
import com.Artiom.ArtifexAI.Media.DTO.AlbumDTO;
import com.Artiom.ArtifexAI.Media.DTO.AlbumEditDTO;
import com.Artiom.ArtifexAI.Media.DTO.AlbumMediaDTO;
import com.Artiom.ArtifexAI.Media.Service.AlbumService;
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
@Tag(name = "album_management")
@RequestMapping("/api/album/v1")
public class AlbumController {
    @Value("${api.token}")
    private String apiToken;

    private final AlbumService albumService;

    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse> deleteAlbum(@RequestHeader("X-auth-token") String token,
                                                   @RequestParam String albumId) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            albumService.deleteAlbum(albumId);
            return ResponseEntity.ok(ApiResponse.success("Delete album successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PostMapping("/create")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = AlbumDTO.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> createAlbum(@RequestHeader("X-auth-token") String token,
                                                   @RequestParam AlbumCreateDTO albumCreateDTO) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            return ResponseEntity.ok(ApiResponse.success("Create album successfully", albumService.createAlbum(albumCreateDTO)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PutMapping("/add_image")
    public ResponseEntity<ApiResponse> addImageToAlbum(@RequestHeader("X-auth-token") String token,
                                                       @RequestBody AlbumMediaDTO albumMediaDTO) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            albumService.addMediaToAlbum(albumMediaDTO);
            return ResponseEntity.ok(ApiResponse.success("Add image to album successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PutMapping("/delete_image")
    public ResponseEntity<ApiResponse> deleteImageFromAlbum(@RequestHeader("X-auth-token") String token,
                                                            @RequestBody AlbumMediaDTO albumMediaDTO) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            albumService.removeMediaFromAlbum(albumMediaDTO);
            return ResponseEntity.ok(ApiResponse.success("Delete image from album successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PutMapping("/edit")
    public ResponseEntity<ApiResponse> editAlbum(@RequestHeader("X-auth-token") String token,
                                                 @RequestBody AlbumEditDTO albumEditDTO) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            albumService.editAlbum(albumEditDTO);
            return ResponseEntity.ok(ApiResponse.success("Edit album successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @GetMapping("/get_by_id")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(implementation = AlbumDTO.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> getAlbumById(@RequestHeader("X-auth-token") String token,
                                                    @RequestParam String albumId) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            return ResponseEntity.ok(ApiResponse.success("Fetch album successfully", albumService.getAlbumById(albumId)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @GetMapping("/get_all")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = {@Content(array = @ArraySchema(schema = @Schema(implementation = AlbumDTO.class)), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> getAllAlbums(@RequestHeader("X-auth-token") String token) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            return ResponseEntity.ok(ApiResponse.success("Fetch albums successfully", albumService.getAllAlbums()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }
}
