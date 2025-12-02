package com.Artiom.ArtifexAI.ImageGeneration.Service.Impl;

import autovalue.shaded.com.google.common.collect.ImmutableList;
import com.Artiom.ArtifexAI.Common.Exception.BusinessException;
import com.Artiom.ArtifexAI.Media.DTO.MediaDTO;
import com.Artiom.ArtifexAI.Media.Model.MediaType;
import com.Artiom.ArtifexAI.Media.Service.AlbumService;
import com.Artiom.ArtifexAI.Media.Service.MediaService;
import com.Artiom.ArtifexAI.ImageGeneration.DTO.*;
import com.Artiom.ArtifexAI.ImageGeneration.Service.ImageGenerationService;
import com.Artiom.ArtifexAI.Persistence.Service.PersistenceService;
import com.Artiom.ArtifexAI.Project.Model.Project;
import com.Artiom.ArtifexAI.Project.Repository.ProjectRepository;
import com.Artiom.ArtifexAI.PromptOptimization.Model.PromptType;
import com.Artiom.ArtifexAI.PromptOptimization.Service.Optimization.PromptOptimizationService;
import com.Artiom.ArtifexAI.PromptOptimization.Service.Template.PromptTemplateService;
import com.Artiom.ArtifexAI.Util.AuthenticationUtils;
import com.google.genai.Client;
import com.google.genai.types.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImageGenerationServiceImpl implements ImageGenerationService {
    @Value("${gemini.imageModel}")
    private String modelName;

    private ImmutableList<SafetySetting> safetySettings;

    private final MediaService mediaService;
    private final AlbumService albumService;
    private final ProjectRepository projectRepository;
    private final PromptOptimizationService promptOptimizationService;
    private final PromptTemplateService promptTemplateService;
    private final PersistenceService persistenceService;
    private final Client client;

    @PostConstruct
    private void buildSafetySettings() {
        safetySettings = ImmutableList.of(
                SafetySetting.builder()
                        .category(HarmCategory.Known.HARM_CATEGORY_IMAGE_DANGEROUS_CONTENT)
                        .threshold(HarmBlockThreshold.Known.BLOCK_NONE)
                        .build(),
                SafetySetting.builder()
                        .category(HarmCategory.Known.HARM_CATEGORY_IMAGE_HARASSMENT)
                        .threshold(HarmBlockThreshold.Known.BLOCK_NONE)
                        .build(),
                SafetySetting.builder()
                        .category(HarmCategory.Known.HARM_CATEGORY_IMAGE_HATE)
                        .threshold(HarmBlockThreshold.Known.BLOCK_NONE)
                        .build(),
                SafetySetting.builder()
                        .category(HarmCategory.Known.HARM_CATEGORY_IMAGE_SEXUALLY_EXPLICIT)
                        .threshold(HarmBlockThreshold.Known.BLOCK_NONE)
                        .build());
    }

    private Content getSystemInstruction() {
        Content systemInstruction = Content.builder()
                .parts(Part.fromText("You are a professional game artist creating 2D game arts based on description."))
                .build();

        return systemInstruction;
    }

    @Override
    public ImageGenerationResponse generateSplashArt(SplashArtGenerationRequest request) {
        List<String> pathList = new java.util.ArrayList<>();

        Project project = getAndCheckProject(request.getProjectId());
        String context = String.join(";", project.getInstructions());

        String optimizedPrompt = promptOptimizationService.optimizePrompt(request.getSplashDescription());

        String promptContent = promptTemplateService.getTemplate(PromptType.SPLASH_ART_GENERATION);
        promptContent = promptContent.replace("{CONTEXT}", context);
        promptContent = promptContent.replace("{ART_STYLE}", project.getArtStyle().toString());
        promptContent = promptContent.replace("{SPLASH_ART_DESCRIPTION}", optimizedPrompt);

        GenerateContentConfig contentConfig = GenerateContentConfig.builder()
                .responseModalities("IMAGE")
                .candidateCount(request.getNumberOfOutputs())
                .safetySettings(safetySettings)
                .systemInstruction(getSystemInstruction())
                .mediaResolution(MediaResolution.Known.MEDIA_RESOLUTION_HIGH)
                .build();

        GenerateContentResponse response =
                client.models.generateContent(
                        modelName,
                        promptContent,
                        contentConfig);

        List<byte[]> imageData = new ArrayList<>();

        for (Candidate candidate : response.candidates().orElse(Collections.emptyList())) {
            candidate.content().flatMap(Content::parts).ifPresent(parts -> {
                for (Part part : parts) {
                    part.inlineData()
                            .flatMap(Blob::data)
                            .ifPresent(data -> {
                                imageData.add(data);
                                String outputPath = persistenceService.uploadServerImageToPersistence(data);
                                if (!outputPath.isEmpty()) {
                                    MediaDTO image = mediaService.addServerMedia(outputPath, MediaType.IMAGE);
                                    albumService.addMediaToProjectAlbum(image.getId(), request.getProjectId());
                                    pathList.add(persistenceService.getMediaUrl(outputPath));
                                }
                            });
                }
            });
        }

        String additionalInstruction = promptOptimizationService.analyzePromptAndImages(optimizedPrompt, imageData, project.getInstructions());

        if (additionalInstruction != null && !additionalInstruction.isEmpty() && !additionalInstruction.equals("N/A")) {
            project.getInstructions().add(additionalInstruction);
            projectRepository.save(project);
        }

        return ImageGenerationResponse.builder()
                .imageUrls(pathList)
                .updatedInstruction(additionalInstruction)
                .build();
    }

    @Override
    public ImageGenerationResponse generateImageVariation(ImageEditRequest request) {
        List<String> pathList = new java.util.ArrayList<>();

        Project project = getAndCheckProject(request.getProjectId());
        String context = String.join(";", project.getInstructions());

        String optimizedPrompt = promptOptimizationService.optimizePrompt(request.getPrompt());

        String promptContent = promptTemplateService.getTemplate(PromptType.IMAGE_EDIT);
        promptContent = promptContent.replace("{CONTEXT}", context);
        promptContent = promptContent.replace("{ART_STYLE}", project.getArtStyle().toString());
        promptContent = promptContent.replace("{PROMPT}", optimizedPrompt);


        GenerateContentConfig contentConfig = GenerateContentConfig.builder()
                .responseModalities("IMAGE")
                .candidateCount(request.getNumberOfOutputs())
                .safetySettings(safetySettings)
                .systemInstruction(getSystemInstruction())
                .mediaResolution(MediaResolution.Known.MEDIA_RESOLUTION_HIGH)
                .build();

        List<Part> parts = new ArrayList<>();
        parts.add(Part.fromText(promptContent));

        for (ImageInfo imageInfo : request.getImageInfos()) {
            byte[] imageByte = persistenceService.downloadImageFromPersistence(imageInfo.getImagePath());
            parts.add(Part.fromBytes(imageByte, imageInfo.getMimeType().getValue()));
        }

        Content content = Content.fromParts(parts.toArray(new Part[0]));

        GenerateContentResponse response =
                client.models.generateContent(
                        modelName,
                        content,
                        contentConfig);

        List<byte[]> imageData = new ArrayList<>();

        for (Candidate candidate : response.candidates().orElse(Collections.emptyList())) {
            candidate.content().flatMap(Content::parts).ifPresent(partList -> {
                for (Part part : parts) {
                    part.inlineData()
                            .flatMap(Blob::data)
                            .ifPresent(data -> {
                                imageData.add(data);
                                String outputPath = persistenceService.uploadServerImageToPersistence(data);
                                if (!outputPath.isEmpty()) {
                                    MediaDTO image = mediaService.addServerMedia(outputPath, MediaType.IMAGE);
                                    albumService.addMediaToProjectAlbum(image.getId(), request.getProjectId());
                                    pathList.add(persistenceService.getMediaUrl(outputPath));
                                }
                            });
                }
            });
        }

        String additionalInstruction = promptOptimizationService.analyzePromptAndImages(optimizedPrompt, imageData, project.getInstructions());

        if (additionalInstruction != null && !additionalInstruction.isEmpty() && !additionalInstruction.equals("N/A")) {
            project.getInstructions().add(additionalInstruction);
            projectRepository.save(project);
        }

        return ImageGenerationResponse.builder()
                .imageUrls(pathList)
                .updatedInstruction(additionalInstruction)
                .build();
    }

    @Override
    public ImageGenerationResponse generateSpriteSheet(SpriteSheetGenerationRequest request) {
        List<String> pathList = new java.util.ArrayList<>();

        Project project = getAndCheckProject(request.getProjectId());
        String context = String.join(";", project.getInstructions());

        String additionalCharacterDescription = request.getCharacterDescription();
        String optimizedCharacterDescription = (additionalCharacterDescription != null && !additionalCharacterDescription.isEmpty()) ? promptOptimizationService.optimizePrompt(additionalCharacterDescription) : "No character description.";

        String additionalActionDescription = request.getActionDescription();
        String optimizedActionDescription = (additionalActionDescription != null && !additionalActionDescription.isEmpty()) ? promptOptimizationService.optimizePrompt(additionalActionDescription) : "No action description.";

        String promptContent = promptTemplateService.getTemplate(PromptType.SPRITE_SHEET_GENERATION);
        promptContent = promptContent.replace("{CONTEXT}", context);
        promptContent = promptContent.replace("{NEW_ART_STYLE}", project.getArtStyle().toString());
        promptContent = promptContent.replace("{CHARACTER_DESCRIPTION}", optimizedCharacterDescription);
        promptContent = promptContent.replace("{CHARACTER_ACTION}", optimizedActionDescription);

        GenerateContentConfig contentConfig = GenerateContentConfig.builder()
                .responseModalities("IMAGE")
                .candidateCount(request.getNumberOfOutputs())
                .safetySettings(safetySettings)
                .systemInstruction(getSystemInstruction())
                .mediaResolution(MediaResolution.Known.MEDIA_RESOLUTION_HIGH)
                .build();

        List<Part> parts = new ArrayList<>();
        parts.add(Part.fromText(promptContent));

        for (ImageInfo imageInfo : request.getImageInfos()) {
            byte[] imageByte = persistenceService.downloadImageFromPersistence(imageInfo.getImagePath());
            parts.add(Part.fromBytes(imageByte, imageInfo.getMimeType().getValue()));
        }

        Content content = Content.fromParts(parts.toArray(new Part[0]));

        GenerateContentResponse response =
                client.models.generateContent(
                        modelName,
                        content,
                        contentConfig);

        List<byte[]> imageData = new ArrayList<>();

        for (Candidate candidate : response.candidates().orElse(Collections.emptyList())) {
            candidate.content().flatMap(Content::parts).ifPresent(partList -> {
                for (Part part : parts) {
                    part.inlineData()
                            .flatMap(Blob::data)
                            .ifPresent(data -> {
                                imageData.add(data);
                                String outputPath = persistenceService.uploadServerImageToPersistence(data);
                                if (!outputPath.isEmpty()) {
                                    MediaDTO image = mediaService.addServerMedia(outputPath, MediaType.IMAGE);
                                    albumService.addMediaToProjectAlbum(image.getId(), request.getProjectId());
                                    pathList.add(persistenceService.getMediaUrl(outputPath));
                                }
                            });
                }
            });
        }

        String additionalInstruction = promptOptimizationService.analyzePromptAndImages(optimizedCharacterDescription, imageData, project.getInstructions());

        if (additionalInstruction != null && !additionalInstruction.isEmpty() && !additionalInstruction.equals("N/A")) {
            project.getInstructions().add(additionalInstruction);
            projectRepository.save(project);
        }

        return ImageGenerationResponse.builder()
                .imageUrls(pathList)
                .updatedInstruction(additionalInstruction)
                .build();
    }


    @Override
    public ImageGenerationResponse changeImageStyle(ImageStyleChangeRequest request) {
        List<String> pathList = new java.util.ArrayList<>();

        Project project = getAndCheckProject(request.getProjectId());
        String context = String.join(";", project.getInstructions());

        String additionalPrompt = request.getAdditionalPrompts();

        String optimizedPrompt = (additionalPrompt != null && !additionalPrompt.isEmpty()) ? promptOptimizationService.optimizePrompt(additionalPrompt) : "No further instructions.";

        String promptContent = promptTemplateService.getTemplate(PromptType.IMAGE_CHANGE_ART_STYLE);
        promptContent = promptContent.replace("{CONTEXT}", context);
        promptContent = promptContent.replace("{NEW_ART_STYLE}", request.getTargetStyle().toString());
        promptContent = promptContent.replace("{PROMPT}", optimizedPrompt);

        GenerateContentConfig contentConfig = GenerateContentConfig.builder()
                .responseModalities("IMAGE")
                .candidateCount(request.getNumberOfOutputs())
                .safetySettings(safetySettings)
                .systemInstruction(getSystemInstruction())
                .mediaResolution(MediaResolution.Known.MEDIA_RESOLUTION_HIGH)
                .build();

        Content content = Content.fromParts(
                Part.fromText(promptContent),
                Part.fromBytes(persistenceService.downloadImageFromPersistence(request.getImageInfo().getImagePath()), request.getImageInfo().getMimeType().getValue())
        );

        GenerateContentResponse response =
                client.models.generateContent(
                        modelName,
                        content,
                        contentConfig);

        List<byte[]> imageData = new ArrayList<>();

        for (Candidate candidate : response.candidates().orElse(Collections.emptyList())) {
            candidate.content().flatMap(Content::parts).ifPresent(parts -> {
                for (Part part : parts) {
                    part.inlineData()
                            .flatMap(Blob::data)
                            .ifPresent(data -> {
                                imageData.add(data);
                                String outputPath = persistenceService.uploadServerImageToPersistence(data);
                                if (!outputPath.isEmpty()) {
                                    MediaDTO image = mediaService.addServerMedia(outputPath, MediaType.IMAGE);
                                    albumService.addMediaToProjectAlbum(image.getId(), request.getProjectId());
                                    pathList.add(persistenceService.getMediaUrl(outputPath));
                                }
                            });
                }
            });
        }

        String additionalInstruction = promptOptimizationService.analyzePromptAndImages(optimizedPrompt, imageData, project.getInstructions());

        if (additionalInstruction != null && !additionalInstruction.isEmpty() && !additionalInstruction.equals("N/A")) {
            project.getInstructions().add(additionalInstruction);
            projectRepository.save(project);
        }

        return ImageGenerationResponse.builder()
                .imageUrls(pathList)
                .updatedInstruction(additionalInstruction)
                .build();
    }

    private Project getAndCheckProject(String projectId) {
        if(projectId == null || projectId.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "You can't generate images without a project");
        }

        Project project = projectRepository.findById(projectId).orElse(null);

        if(project == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Project doesn't exist");
        }

        if(!project.getUserId().equals(AuthenticationUtils.getCurrentUser().getId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "You are not the owner of this project");
        }

        return project;
    }
}
