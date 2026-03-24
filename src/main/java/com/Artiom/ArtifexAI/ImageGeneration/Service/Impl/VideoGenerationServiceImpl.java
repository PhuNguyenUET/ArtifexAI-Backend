package com.Artiom.ArtifexAI.ImageGeneration.Service.Impl;

import com.Artiom.ArtifexAI.Common.Exception.BusinessException;
import com.Artiom.ArtifexAI.ImageGeneration.DTO.VideoGenerationRequest;
import com.Artiom.ArtifexAI.ImageGeneration.DTO.VideoGenerationResponse;
import com.Artiom.ArtifexAI.ImageGeneration.Service.VideoGenerationService;
import com.Artiom.ArtifexAI.Media.DTO.MediaDTO;
import com.Artiom.ArtifexAI.Media.Model.MediaType;
import com.Artiom.ArtifexAI.Media.Service.AlbumService;
import com.Artiom.ArtifexAI.Media.Service.MediaService;
import com.Artiom.ArtifexAI.Persistence.Service.PersistenceService;
import com.Artiom.ArtifexAI.Project.Model.Project;
import com.Artiom.ArtifexAI.Project.Repository.ProjectRepository;
import com.Artiom.ArtifexAI.PromptOptimization.Model.PromptType;
import com.Artiom.ArtifexAI.PromptOptimization.Service.Optimization.PromptOptimizationService;
import com.Artiom.ArtifexAI.PromptOptimization.Service.Template.PromptTemplateService;
import com.Artiom.ArtifexAI.Util.AuthenticationUtils;
import com.google.genai.Client;
import com.google.genai.types.GenerateVideosConfig;
import com.google.genai.types.GenerateVideosOperation;
import com.google.genai.types.GenerateVideosResponse;
import com.google.genai.types.Image;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoGenerationServiceImpl implements VideoGenerationService {
    @Value("${gemini.videoModel}")
    private String modelName;

    private final MediaService mediaService;
    private final AlbumService albumService;
    private final ProjectRepository projectRepository;
    private final PromptOptimizationService promptOptimizationService;
    private final PromptTemplateService promptTemplateService;
    private final PersistenceService persistenceService;
    private final Client client;

    @Override
    public VideoGenerationResponse generateVideo(VideoGenerationRequest request) {
        final String[] videoPath = {null};

        Project project = getAndCheckProject(request.getProjectId());
        String context = String.join(";", project.getInstructions());

        String optimizedPrompt = promptOptimizationService.optimizePrompt(request.getPrompt());

        String promptContent = promptTemplateService.getTemplate(PromptType.VIDEO_GENERATION);
        promptContent = promptContent.replace("{CONTEXT}", context);
        promptContent = promptContent.replace("{ART_STYLE}", project.getArtStyle().toString());
        promptContent = promptContent.replace("{VIDEO_DESCRIPTION}", optimizedPrompt);

        int seconds = switch (request.getVideoLength()) {
            case SHORT -> 4;
            case MEDIUM -> 6;
            case LONG -> 8;
        };

        GenerateVideosConfig config = GenerateVideosConfig.builder()
                .numberOfVideos(1)
                .enhancePrompt(true)
                .durationSeconds(seconds)
                .build();

        GenerateVideosOperation operation;

        if (request.getReferenceImage() != null) {
            byte[] imageByte = persistenceService.downloadImageFromPersistence(request.getReferenceImage().getImagePath());

            Image image = Image.builder().imageBytes(imageByte).build();
            operation = client.models.generateVideos(
                    modelName,
                    promptContent,
                    image,
                    config);
        } else {
            operation = client.models.generateVideos(
                    modelName,
                    promptContent,
                    null,
                    config);
        }

        while (operation.done().isEmpty()) {
            try {
                Thread.sleep(5000);
                operation = client.operations.getVideosOperation(operation, null);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Video generation was interrupted");
            }
        }

        operation.response().flatMap(GenerateVideosResponse::generatedVideos).ifPresent(videos -> {
            if (!videos.isEmpty()) {
                videos.get(0).video().ifPresent(video -> {
                    try {
                        byte[] videoData = video.videoBytes().orElse(null);
                        String outputPath = persistenceService.uploadServerVideoToPersistence(videoData);
                        if (!outputPath.isEmpty()) {
                            videoPath[0] = outputPath;
                            MediaDTO videoMedia = mediaService.addServerMedia(outputPath, MediaType.VIDEO);
                            albumService.addMediaToProjectAlbum(videoMedia.getId(), MediaType.VIDEO, request.getProjectId());
                        }
                    } catch (Exception e) {
                        throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save generated video: " + e.getMessage());
                    }
                });
            }
        });

        String additionalInstruction = promptOptimizationService.analyzePromptAndImages(
                optimizedPrompt,
                new ArrayList<>(),
                project.getInstructions());

        if (additionalInstruction != null && !additionalInstruction.isEmpty() && !additionalInstruction.equals("N/A")) {
            project.getInstructions().add(additionalInstruction);
            projectRepository.save(project);
        }

        String videoUrl = videoPath[0] != null ? persistenceService.getMediaUrl(videoPath[0]) : null;

        return VideoGenerationResponse.builder()
                .videoUrl(videoUrl)
                .updatedInstruction(additionalInstruction)
                .build();
    }

    private Project getAndCheckProject(String projectId) {
        if (projectId == null || projectId.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "You can't generate videos without a project");
        }

        Project project = projectRepository.findById(projectId).orElse(null);

        if (project == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Project doesn't exist");
        }

        if (!project.getUserId().equals(AuthenticationUtils.getCurrentUser().getId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "You are not the owner of this project");
        }

        return project;
    }
}
