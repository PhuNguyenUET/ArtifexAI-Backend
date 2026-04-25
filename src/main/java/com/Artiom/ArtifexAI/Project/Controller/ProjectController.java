package com.Artiom.ArtifexAI.Project.Controller;

import com.Artiom.ArtifexAI.Common.Api.ApiResponse;
import com.Artiom.ArtifexAI.Project.DTO.ProjectCreateDTO;
import com.Artiom.ArtifexAI.Project.DTO.ProjectDTO;
import com.Artiom.ArtifexAI.Project.DTO.ProjectEditDTO;
import com.Artiom.ArtifexAI.Project.DTO.ProjectInstructionUpdateDTO;
import com.Artiom.ArtifexAI.Project.Service.ProjectService;
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
@Tag(name = "project_management")
@RequestMapping("/api/project/v1")
public class ProjectController {
    @Value("${api.token}")
    private String apiToken;

    private final ProjectService projectService;

    @PostMapping("/create")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = ProjectDTO.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> createProject(@RequestHeader("X-auth-token") String token,
                                                     @RequestBody ProjectCreateDTO projectCreateDTO) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            return ResponseEntity.ok(ApiResponse.success("Create project successfully", projectService.createProject(projectCreateDTO)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse> deleteProject(@RequestHeader("X-auth-token") String token,
                                                     @RequestParam Long projectId) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            projectService.deleteProject(projectId);
            return ResponseEntity.ok(ApiResponse.success("Delete project successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PutMapping("/edit")
    public ResponseEntity<ApiResponse> editProject (@RequestHeader("X-auth-token") String token,
                                                    @RequestBody ProjectEditDTO projectEditDTO) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            projectService.editProject(projectEditDTO);
            return ResponseEntity.ok(ApiResponse.success("Edit project successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PutMapping("/update_instructions")
    public ResponseEntity<ApiResponse> updateInstructions (@RequestHeader("X-auth-token") String token,
                                                           @RequestBody ProjectInstructionUpdateDTO projectInstructionUpdateDTO) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            projectService.updateInstructionList(projectInstructionUpdateDTO);
            return ResponseEntity.ok(ApiResponse.success("Update project instructions successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PutMapping("/add_instructions")
    public ResponseEntity<ApiResponse> addInstructions (@RequestHeader("X-auth-token") String token,
                                                        @RequestBody ProjectInstructionUpdateDTO projectInstructionUpdateDTO) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            projectService.addInstructionString(projectInstructionUpdateDTO);
            return ResponseEntity.ok(ApiResponse.success("Add project instructions successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @GetMapping("/get_by_id")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = ProjectDTO.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> getProjectById (@RequestHeader("X-auth-token") String token,
                                                       @RequestParam Long projectId) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            return ResponseEntity.ok(ApiResponse.success("Fetch project successfully", projectService.getProject(projectId)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @GetMapping("/get_all")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = {@Content(array = @ArraySchema(schema = @Schema(implementation = ProjectDTO.class)), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> getAllProjects (@RequestHeader("X-auth-token") String token) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            return ResponseEntity.ok(ApiResponse.success("Fetch projects successfully", projectService.getALlProjects()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }
}
