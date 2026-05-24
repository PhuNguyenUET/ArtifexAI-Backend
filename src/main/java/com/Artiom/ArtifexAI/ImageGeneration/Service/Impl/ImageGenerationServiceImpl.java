package com.Artiom.ArtifexAI.ImageGeneration.Service.Impl;

import autovalue.shaded.com.google.common.collect.ImmutableList;
import com.Artiom.ArtifexAI.Common.Exception.BusinessException;
import com.Artiom.ArtifexAI.FalAI.FalAIService;
import com.Artiom.ArtifexAI.ImageGeneration.DTO.*;
import com.Artiom.ArtifexAI.ImageGeneration.Service.ImageGenerationService;
import com.Artiom.ArtifexAI.Media.DTO.MediaDTO;
import com.Artiom.ArtifexAI.Media.Model.MediaType;
import com.Artiom.ArtifexAI.Media.Service.AlbumService;
import com.Artiom.ArtifexAI.Media.Service.MediaService;
import com.Artiom.ArtifexAI.Persistence.Service.PersistenceService;
import com.Artiom.ArtifexAI.Project.Model.ArtStyle;
import com.Artiom.ArtifexAI.Project.Model.Project;
import com.Artiom.ArtifexAI.Project.Repository.ProjectRepository;
import com.Artiom.ArtifexAI.PromptOptimization.Model.PromptType;
import com.Artiom.ArtifexAI.PromptOptimization.Service.Optimization.PromptOptimizationService;
import com.Artiom.ArtifexAI.PromptOptimization.Service.Template.PromptTemplateService;
import com.Artiom.ArtifexAI.PromptOptimization.Service.Template.StyleTemplateService;
import com.Artiom.ArtifexAI.User.Model.User;
import com.Artiom.ArtifexAI.User.Repository.UserRepository;
import com.Artiom.ArtifexAI.Util.AuthenticationUtils;
import com.google.genai.Client;
import com.google.genai.types.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ImageGenerationServiceImpl implements ImageGenerationService {
    @Value("${gemini.imageModel}")
    private String modelName;

    @Value("${gemini.upscaleModel}")
    private String upscaleModelName;

    @Value("${gemini.maskEditModel}")
    private String maskEditModelName;

    private ImmutableList<SafetySetting> safetySettings;

    private final MediaService mediaService;
    private final AlbumService albumService;
    private final ProjectRepository projectRepository;
    private final PromptOptimizationService promptOptimizationService;
    private final PromptTemplateService promptTemplateService;
    private final StyleTemplateService styleTemplateService;
    private final PersistenceService persistenceService;
    private final Client client;
    private final FalAIService falAIService;
    private final UserRepository userRepository;

    @PostConstruct
    private void buildSafetySettings() {
        safetySettings = ImmutableList.of(
                SafetySetting.builder()
                        .category(HarmCategory.Known.HARM_CATEGORY_IMAGE_DANGEROUS_CONTENT)
                        .threshold(HarmBlockThreshold.Known.BLOCK_ONLY_HIGH)
                        .build(),
                SafetySetting.builder()
                        .category(HarmCategory.Known.HARM_CATEGORY_IMAGE_HARASSMENT)
                        .threshold(HarmBlockThreshold.Known.BLOCK_ONLY_HIGH)
                        .build(),
                SafetySetting.builder()
                        .category(HarmCategory.Known.HARM_CATEGORY_IMAGE_HATE)
                        .threshold(HarmBlockThreshold.Known.BLOCK_ONLY_HIGH)
                        .build(),
                SafetySetting.builder()
                        .category(HarmCategory.Known.HARM_CATEGORY_IMAGE_SEXUALLY_EXPLICIT)
                        .threshold(HarmBlockThreshold.Known.BLOCK_ONLY_HIGH)
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
        promptContent = promptContent.replace("{ART_STYLE}", resolveArtStyle(project.getArtStyle()));
        promptContent = promptContent.replace("{SPLASH_ART_DESCRIPTION}", optimizedPrompt);

        GenerateContentConfig contentConfig = GenerateContentConfig.builder()
                .responseModalities("IMAGE")
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
                                    albumService.addMediaToProjectAlbum(image.getId(), MediaType.IMAGE, request.getProjectId());
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
    public ImageGenerationResponse generateImageVariation(ImageVariationRequest request) {
        List<String> pathList = new java.util.ArrayList<>();

        Project project = getAndCheckProject(request.getProjectId());
        String context = String.join(";", project.getInstructions());

        String optimizedPrompt = promptOptimizationService.optimizePrompt(request.getPrompt());

        String promptContent = promptTemplateService.getTemplate(PromptType.IMAGE_EDIT);
        promptContent = promptContent.replace("{CONTEXT}", context);
        promptContent = promptContent.replace("{ART_STYLE}", resolveArtStyle(project.getArtStyle()));
        promptContent = promptContent.replace("{PROMPT}", optimizedPrompt);


        GenerateContentConfig contentConfig = GenerateContentConfig.builder()
                .responseModalities("IMAGE")
                .safetySettings(safetySettings)
                .systemInstruction(getSystemInstruction())
                .mediaResolution(MediaResolution.Known.MEDIA_RESOLUTION_HIGH)
                .build();

        List<Part> parts = new ArrayList<>();
        parts.add(Part.fromText(promptContent));

        for (ImageInfo imageInfo : request.getImageInfos()) {
            byte[] imageByte = persistenceService.downloadImageFromPersistence(imageInfo.getImagePath());
            if (imageByte != null && imageByte.length > 0) {
                parts.add(Part.fromBytes(imageByte, imageInfo.getMimeType().getValue()));
            }
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
                for (Part part : partList) {
                    part.inlineData()
                            .flatMap(Blob::data)
                            .ifPresent(data -> {
                                imageData.add(data);
                                String outputPath = persistenceService.uploadServerImageToPersistence(data);
                                if (!outputPath.isEmpty()) {
                                    MediaDTO image = mediaService.addServerMedia(outputPath, MediaType.IMAGE);
                                    albumService.addMediaToProjectAlbum(image.getId(), MediaType.IMAGE, request.getProjectId());
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
        promptContent = promptContent.replace("{ART_STYLE}", resolveArtStyle(project.getArtStyle()));
        promptContent = promptContent.replace("{CHARACTER_DESCRIPTION}", optimizedCharacterDescription);
        promptContent = promptContent.replace("{CHARACTER_ACTION}", optimizedActionDescription);

        GenerateContentConfig contentConfig = GenerateContentConfig.builder()
                .responseModalities("IMAGE")
                .safetySettings(safetySettings)
                .systemInstruction(getSystemInstruction())
                .mediaResolution(MediaResolution.Known.MEDIA_RESOLUTION_HIGH)
                .build();

        List<Part> parts = new ArrayList<>();
        parts.add(Part.fromText(promptContent));

        for (ImageInfo imageInfo : request.getImageInfos()) {
            byte[] imageByte = persistenceService.downloadImageFromPersistence(imageInfo.getImagePath());
            if (imageByte != null && imageByte.length > 0) {
                parts.add(Part.fromBytes(imageByte, imageInfo.getMimeType().getValue()));
            }
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
                for (Part part : partList) {
                    part.inlineData()
                            .flatMap(Blob::data)
                            .ifPresent(data -> {
                                imageData.add(data);
                                String outputPath = persistenceService.uploadServerImageToPersistence(data);
                                if (!outputPath.isEmpty()) {
                                    MediaDTO image = mediaService.addServerMedia(outputPath, MediaType.IMAGE);
                                    albumService.addMediaToProjectAlbum(image.getId(), MediaType.IMAGE, request.getProjectId());
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

        String additionalPrompt = request.getAdditionalPrompts();

        String optimizedPrompt = (additionalPrompt != null && !additionalPrompt.isEmpty()) ? promptOptimizationService.optimizePrompt(additionalPrompt) : "No further instructions.";

        String promptContent = promptTemplateService.getTemplate(PromptType.IMAGE_CHANGE_ART_STYLE);
        promptContent = promptContent.replace("{NEW_ART_STYLE}", resolveArtStyle(request.getTargetStyle()));
        promptContent = promptContent.replace("{PROMPT}", optimizedPrompt);

        GenerateContentConfig contentConfig = GenerateContentConfig.builder()
                .responseModalities("IMAGE")
                .safetySettings(safetySettings)
                .systemInstruction(getSystemInstruction())
                .mediaResolution(MediaResolution.Known.MEDIA_RESOLUTION_HIGH)
                .build();

        List<Part> contentParts = new ArrayList<>();
        contentParts.add(Part.fromText(promptContent));
        byte[] referenceImageByte = persistenceService.downloadImageFromPersistence(request.getImageInfo().getImagePath());
        if (referenceImageByte != null && referenceImageByte.length > 0) {
            contentParts.add(Part.fromBytes(referenceImageByte, request.getImageInfo().getMimeType().getValue()));
        }

        Content content = Content.fromParts(contentParts.toArray(new Part[0]));

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
                                    albumService.addMediaToProjectAlbum(image.getId(), MediaType.IMAGE, request.getProjectId());
                                    pathList.add(persistenceService.getMediaUrl(outputPath));
                                }
                            });
                }
            });
        }

        return ImageGenerationResponse.builder()
                .imageUrls(pathList)
                .updatedInstruction("N/A")
                .build();
    }

    @Override
    public ImageGenerationResponse editImageWithImageMask(ImageEditWithMaskRequest request) {
        List<String> pathList = new java.util.ArrayList<>();

        Project project = getAndCheckProject(request.getProjectId());

        String editPrompt = request.getPrompt();

        String optimizedPrompt = (editPrompt != null && !editPrompt.isEmpty()) ? promptOptimizationService.optimizePrompt(editPrompt) : "No further instructions.";

        String promptContent = promptTemplateService.getTemplate(PromptType.IMAGE_MASKED_EDIT);
        promptContent = promptContent.replace("{ART_STYLE}", resolveArtStyle(project.getArtStyle()));
        promptContent = promptContent.replace("{PROMPT}", optimizedPrompt);

        byte[] imageByte = persistenceService.downloadImageFromPersistence(request.getImageInfo().getImagePath());
        if (imageByte == null || imageByte.length == 0) {
            return ImageGenerationResponse.builder().imageUrls(pathList).updatedInstruction("N/A").build();
        }

        Image referenceImage = Image.builder().imageBytes(imageByte).build();

        EditImageConfig config =
                EditImageConfig.builder()
                        .safetyFilterLevel(SafetyFilterLevel.Known.BLOCK_ONLY_HIGH)
                        .editMode(request.getEditMode())
                        .numberOfImages(3)
                        .outputMimeType("image/png")
                        .build();

        ArrayList<ReferenceImage> referenceImages = new ArrayList<>();

        RawReferenceImage rawReferenceImage =
                RawReferenceImage.builder().referenceImage(referenceImage).referenceId(1).build();
        referenceImages.add(rawReferenceImage);

        byte[] maskImageData = Base64.getDecoder().decode(request.getMaskImageBase64());

        MaskReferenceImage maskReferenceImage =
                MaskReferenceImage.builder()
                        .referenceImage(Image.builder().imageBytes(maskImageData).build())
                        .referenceId(2)
                        .config(
                                MaskReferenceConfig.builder()
                                        .maskMode(MaskReferenceMode.Known.MASK_MODE_USER_PROVIDED)
                                        .maskDilation(0.1f))
                        .build();
        referenceImages.add(maskReferenceImage);

        EditImageResponse response = client.models.editImage(
                maskEditModelName,
                promptContent,
                referenceImages,
                config
        );

        List<byte[]> imageData = new ArrayList<>();

        response.generatedImages().ifPresent(images -> {
            for (GeneratedImage generatedImage : images) {
                generatedImage.image().flatMap(Image::imageBytes).ifPresent(data -> {
                    imageData.add(data);
                    String outputPath = persistenceService.uploadServerImageToPersistence(data);
                    if (!outputPath.isEmpty()) {
                        MediaDTO media = mediaService.addServerMedia(outputPath, MediaType.IMAGE);
                        albumService.addMediaToProjectAlbum(media.getId(), MediaType.IMAGE, request.getProjectId());
                        pathList.add(persistenceService.getMediaUrl(outputPath));
                    }
                });
            }
        });

        return ImageGenerationResponse.builder()
                .imageUrls(pathList)
                .updatedInstruction("N/A")
                .build();
    }

    @Override
    public ImageGenerationResponse upsaleImage(UpscaleImageRequest request) {
        List<String> pathList = new java.util.ArrayList<>();

        byte[] imageByte = persistenceService.downloadImageFromPersistence(request.getImageInfo().getImagePath());
        if (imageByte == null || imageByte.length == 0) {
            return ImageGenerationResponse.builder().imageUrls(pathList).updatedInstruction("N/A").build();
        }

        Image referenceImage = Image.builder().imageBytes(imageByte).build();

        UpscaleImageConfig config =
                UpscaleImageConfig.builder()
                        .outputMimeType("image/png")
                        .enhanceInputImage(true)
                        .imagePreservationFactor(0.6f)
                        .build();

        UpscaleImageResponse response =
                client.models.upscaleImage(upscaleModelName, referenceImage, "x2", config);

        response.generatedImages().ifPresent(images -> {
            for (GeneratedImage generatedImage : images) {
                generatedImage.image().flatMap(Image::imageBytes).ifPresent(data -> {
                    String outputPath = persistenceService.uploadServerImageToPersistence(data);
                    if (!outputPath.isEmpty()) {
                        MediaDTO media = mediaService.addServerMedia(outputPath, MediaType.IMAGE);
                        albumService.addMediaToProjectAlbum(media.getId(), MediaType.IMAGE, request.getProjectId());
                        pathList.add(persistenceService.getMediaUrl(outputPath));
                    }
                });
            }
        });

        return ImageGenerationResponse.builder()
                .imageUrls(pathList)
                .updatedInstruction("N/A")
                .build();
    }

    @Override
    public ImageGenerationResponse generateSplashArtFlux2(SplashArtGenerationRequest request) {
        List<String> pathList = new java.util.ArrayList<>();

        Project project = getAndCheckProject(request.getProjectId());
        String fullContext = String.join(";", project.getInstructions());

        String optimizedPrompt = promptOptimizationService.optimizePromptForDiffusion(request.getSplashDescription());
        String context = promptOptimizationService.optimizeContextForDiffusion(optimizedPrompt, fullContext);

        String promptContent = promptTemplateService.getTemplate(PromptType.SPLASH_ART_GENERATION_HF);
        promptContent = promptContent.replace("{CONTEXT}", "N/A".equals(context) ? "" : context);
        promptContent = promptContent.replace("{ART_STYLE}", resolveArtStyleHF(project.getArtStyle()));
        promptContent = promptContent.replace("{SPLASH_ART_DESCRIPTION}", optimizedPrompt);

        byte[] imageBytes = falAIService.generateImageFlux(promptContent);

        String outputPath = persistenceService.uploadServerImageToPersistence(imageBytes);
        List<byte[]> imageData = new ArrayList<>();
        if (!outputPath.isEmpty()) {
            imageData.add(imageBytes);
            MediaDTO media = mediaService.addServerMedia(outputPath, MediaType.IMAGE);
            albumService.addMediaToProjectAlbum(media.getId(), MediaType.IMAGE, request.getProjectId());
            pathList.add(persistenceService.getMediaUrl(outputPath));
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
    public ImageGenerationResponse generateImageVariationFlux2(ImageVariationRequest request) {
        List<String> pathList = new java.util.ArrayList<>();

        Project project = getAndCheckProject(request.getProjectId());
        String fullContext = String.join(";", project.getInstructions());

        String optimizedPrompt = promptOptimizationService.optimizePromptForDiffusion(request.getPrompt());
        String context = promptOptimizationService.optimizeContextForDiffusion(request.getPrompt(), fullContext);

        String promptContent = promptTemplateService.getTemplate(PromptType.IMAGE_EDIT_HF);
        promptContent = promptContent.replace("{CONTEXT}", "N/A".equals(context) ? "" : context);
        promptContent = promptContent.replace("{ART_STYLE}", resolveArtStyleHF(project.getArtStyle()));
        promptContent = promptContent.replace("{PROMPT}", optimizedPrompt);

        List<String> imageDataUris = new ArrayList<>();
        if (request.getImageInfos() != null) {
            for (ImageInfo imageInfo : request.getImageInfos()) {
                byte[] imgBytes = persistenceService.downloadImageFromPersistence(imageInfo.getImagePath());
                if (imgBytes != null && imgBytes.length > 0) {
                    String mimeType = imageInfo.getMimeType().getValue();
                    String dataUri = "data:" + mimeType + ";base64," + Base64.getEncoder().encodeToString(imgBytes);
                    imageDataUris.add(dataUri);
                }
            }
        }

        byte[] imageBytes = falAIService.editImageFlux(promptContent, imageDataUris);

        String outputPath = persistenceService.uploadServerImageToPersistence(imageBytes);
        List<byte[]> imageData = new ArrayList<>();
        if (!outputPath.isEmpty()) {
            imageData.add(imageBytes);
            MediaDTO media = mediaService.addServerMedia(outputPath, MediaType.IMAGE);
            albumService.addMediaToProjectAlbum(media.getId(), MediaType.IMAGE, request.getProjectId());
            pathList.add(persistenceService.getMediaUrl(outputPath));
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
    public ImageGenerationResponse generateSpriteSheetFlux2(SpriteSheetGenerationRequest request) {
        List<String> pathList = new java.util.ArrayList<>();

        Project project = getAndCheckProject(request.getProjectId());
        String fullContext = String.join(";", project.getInstructions());

        String additionalCharacterDescription = request.getCharacterDescription();
        String optimizedCharacterDescription = (additionalCharacterDescription != null && !additionalCharacterDescription.isEmpty())
                ? promptOptimizationService.optimizePromptForDiffusion(additionalCharacterDescription)
                : "No character description.";

        String additionalActionDescription = request.getActionDescription();
        String optimizedActionDescription = (additionalActionDescription != null && !additionalActionDescription.isEmpty())
                ? promptOptimizationService.optimizePromptForDiffusion(additionalActionDescription)
                : "No action description.";

        String contextTopic = optimizedCharacterDescription + " " + optimizedActionDescription;
        String context = promptOptimizationService.optimizeContextForDiffusion(contextTopic, fullContext);

        String promptContent = promptTemplateService.getTemplate(PromptType.SPRITE_SHEET_GENERATION_HF);
        promptContent = promptContent.replace("{CONTEXT}", "N/A".equals(context) ? "" : context);
        promptContent = promptContent.replace("{ART_STYLE}", resolveArtStyleHF(project.getArtStyle()));
        promptContent = promptContent.replace("{CHARACTER_DESCRIPTION}", optimizedCharacterDescription);
        promptContent = promptContent.replace("{CHARACTER_ACTION}", optimizedActionDescription);

        List<String> imageDataUris = new ArrayList<>();
        if (request.getImageInfos() != null) {
            for (ImageInfo imageInfo : request.getImageInfos()) {
                byte[] imgBytes = persistenceService.downloadImageFromPersistence(imageInfo.getImagePath());
                if (imgBytes != null && imgBytes.length > 0) {
                    String mimeType = imageInfo.getMimeType().getValue();
                    String dataUri = "data:" + mimeType + ";base64," + Base64.getEncoder().encodeToString(imgBytes);
                    imageDataUris.add(dataUri);
                }
            }
        }

        byte[] imageBytes = imageDataUris.isEmpty()
                ? falAIService.generateImageFlux(promptContent)
                : falAIService.editImageFlux(promptContent, imageDataUris);

        String outputPath = persistenceService.uploadServerImageToPersistence(imageBytes);
        List<byte[]> imageData = new ArrayList<>();
        if (!outputPath.isEmpty()) {
            imageData.add(imageBytes);
            MediaDTO media = mediaService.addServerMedia(outputPath, MediaType.IMAGE);
            albumService.addMediaToProjectAlbum(media.getId(), MediaType.IMAGE, request.getProjectId());
            pathList.add(persistenceService.getMediaUrl(outputPath));
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
    public ImageGenerationResponse changeImageStyleFlux2(ImageStyleChangeRequest request) {
        List<String> pathList = new java.util.ArrayList<>();

        Project project = getAndCheckProject(request.getProjectId());

        String additionalPrompt = request.getAdditionalPrompts();
        String optimizedPrompt = (additionalPrompt != null && !additionalPrompt.isEmpty())
                ? promptOptimizationService.optimizePromptForDiffusion(additionalPrompt)
                : "No further instructions.";

        String promptContent = promptTemplateService.getTemplate(PromptType.IMAGE_CHANGE_ART_STYLE_HF);
        promptContent = promptContent.replace("{NEW_ART_STYLE}", resolveArtStyleHF(request.getTargetStyle()));
        promptContent = promptContent.replace("{PROMPT}", optimizedPrompt);

        // Convert reference image to a base64 data URI for Flux-2
        byte[] imgBytes = persistenceService.downloadImageFromPersistence(request.getImageInfo().getImagePath());
        if (imgBytes == null || imgBytes.length == 0) {
            return ImageGenerationResponse.builder().imageUrls(pathList).updatedInstruction("N/A").build();
        }
        String mimeType = request.getImageInfo().getMimeType().getValue();
        String dataUri = "data:" + mimeType + ";base64," + Base64.getEncoder().encodeToString(imgBytes);

        byte[] imageBytes = falAIService.editImageFlux(promptContent, List.of(dataUri));

        String outputPath = persistenceService.uploadServerImageToPersistence(imageBytes);
        List<byte[]> imageData = new ArrayList<>();
        if (!outputPath.isEmpty()) {
            imageData.add(imageBytes);
            MediaDTO media = mediaService.addServerMedia(outputPath, MediaType.IMAGE);
            albumService.addMediaToProjectAlbum(media.getId(), MediaType.IMAGE, request.getProjectId());
            pathList.add(persistenceService.getMediaUrl(outputPath));
        }

        return ImageGenerationResponse.builder()
                .imageUrls(pathList)
                .updatedInstruction("N/A")
                .build();
    }


    @Override
    public ImageGenerationResponse generateSplashArtQwen(SplashArtGenerationRequest request) {
        List<String> pathList = new java.util.ArrayList<>();

        Project project = getAndCheckProject(request.getProjectId());
        String fullContext = String.join(";", project.getInstructions());

        String optimizedPrompt = promptOptimizationService.optimizePromptForDiffusion(request.getSplashDescription());
        String context = promptOptimizationService.optimizeContextForDiffusion(optimizedPrompt, fullContext);

        String promptContent = promptTemplateService.getTemplate(PromptType.SPLASH_ART_GENERATION_HF);
        promptContent = promptContent.replace("{CONTEXT}", "N/A".equals(context) ? "" : context);
        promptContent = promptContent.replace("{ART_STYLE}", resolveArtStyleHF(project.getArtStyle()));
        promptContent = promptContent.replace("{SPLASH_ART_DESCRIPTION}", optimizedPrompt);

        byte[] imageBytes = falAIService.generateImageQwen(promptContent);

        String outputPath = persistenceService.uploadServerImageToPersistence(imageBytes);
        List<byte[]> imageData = new ArrayList<>();
        if (!outputPath.isEmpty()) {
            imageData.add(imageBytes);
            MediaDTO media = mediaService.addServerMedia(outputPath, MediaType.IMAGE);
            albumService.addMediaToProjectAlbum(media.getId(), MediaType.IMAGE, request.getProjectId());
            pathList.add(persistenceService.getMediaUrl(outputPath));
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
    public ImageGenerationResponse generateImageVariationQwen(ImageVariationRequest request) {
        List<String> pathList = new java.util.ArrayList<>();

        Project project = getAndCheckProject(request.getProjectId());
        String fullContext = String.join(";", project.getInstructions());

        String optimizedPrompt = promptOptimizationService.optimizePromptForDiffusion(request.getPrompt());
        String context = promptOptimizationService.optimizeContextForDiffusion(request.getPrompt(), fullContext);

        String promptContent = promptTemplateService.getTemplate(PromptType.IMAGE_EDIT_HF);
        promptContent = promptContent.replace("{CONTEXT}", "N/A".equals(context) ? "" : context);
        promptContent = promptContent.replace("{ART_STYLE}", resolveArtStyleHF(project.getArtStyle()));
        promptContent = promptContent.replace("{PROMPT}", optimizedPrompt);

        List<String> imageDataUris = new ArrayList<>();
        if (request.getImageInfos() != null) {
            for (ImageInfo imageInfo : request.getImageInfos()) {
                byte[] imgBytes = persistenceService.downloadImageFromPersistence(imageInfo.getImagePath());
                if (imgBytes != null && imgBytes.length > 0) {
                    String mimeType = imageInfo.getMimeType().getValue();
                    imageDataUris.add("data:" + mimeType + ";base64," + Base64.getEncoder().encodeToString(imgBytes));
                }
            }
        }

        byte[] imageBytes = falAIService.editImageQwen(promptContent, imageDataUris);

        String outputPath = persistenceService.uploadServerImageToPersistence(imageBytes);
        List<byte[]> imageData = new ArrayList<>();
        if (!outputPath.isEmpty()) {
            imageData.add(imageBytes);
            MediaDTO media = mediaService.addServerMedia(outputPath, MediaType.IMAGE);
            albumService.addMediaToProjectAlbum(media.getId(), MediaType.IMAGE, request.getProjectId());
            pathList.add(persistenceService.getMediaUrl(outputPath));
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
    public ImageGenerationResponse generateSpriteSheetQwen(SpriteSheetGenerationRequest request) {
        List<String> pathList = new java.util.ArrayList<>();

        Project project = getAndCheckProject(request.getProjectId());
        String fullContext = String.join(";", project.getInstructions());

        String additionalCharacterDescription = request.getCharacterDescription();
        String optimizedCharacterDescription = (additionalCharacterDescription != null && !additionalCharacterDescription.isEmpty())
                ? promptOptimizationService.optimizePromptForDiffusion(additionalCharacterDescription)
                : "No character description.";

        String additionalActionDescription = request.getActionDescription();
        String optimizedActionDescription = (additionalActionDescription != null && !additionalActionDescription.isEmpty())
                ? promptOptimizationService.optimizePromptForDiffusion(additionalActionDescription)
                : "No action description.";

        String contextTopic = optimizedCharacterDescription + " " + optimizedActionDescription;
        String context = promptOptimizationService.optimizeContextForDiffusion(contextTopic, fullContext);

        String promptContent = promptTemplateService.getTemplate(PromptType.SPRITE_SHEET_GENERATION_HF);
        promptContent = promptContent.replace("{CONTEXT}", "N/A".equals(context) ? "" : context);
        promptContent = promptContent.replace("{ART_STYLE}", resolveArtStyleHF(project.getArtStyle()));
        promptContent = promptContent.replace("{CHARACTER_DESCRIPTION}", optimizedCharacterDescription);
        promptContent = promptContent.replace("{CHARACTER_ACTION}", optimizedActionDescription);

        List<String> imageDataUris = new ArrayList<>();
        if (request.getImageInfos() != null) {
            for (ImageInfo imageInfo : request.getImageInfos()) {
                byte[] imgBytes = persistenceService.downloadImageFromPersistence(imageInfo.getImagePath());
                if (imgBytes != null && imgBytes.length > 0) {
                    String mimeType = imageInfo.getMimeType().getValue();
                    imageDataUris.add("data:" + mimeType + ";base64," + Base64.getEncoder().encodeToString(imgBytes));
                }
            }
        }

        if (imageDataUris.isEmpty()) {
            throw new RuntimeException("Qwen image edit requires at least one reference image for sprite sheet generation");
        }

        byte[] imageBytes = falAIService.editImageQwen(promptContent, imageDataUris);

        String outputPath = persistenceService.uploadServerImageToPersistence(imageBytes);
        List<byte[]> imageData = new ArrayList<>();
        if (!outputPath.isEmpty()) {
            imageData.add(imageBytes);
            MediaDTO media = mediaService.addServerMedia(outputPath, MediaType.IMAGE);
            albumService.addMediaToProjectAlbum(media.getId(), MediaType.IMAGE, request.getProjectId());
            pathList.add(persistenceService.getMediaUrl(outputPath));
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
    public ImageGenerationResponse changeImageStyleQwen(ImageStyleChangeRequest request) {
        List<String> pathList = new java.util.ArrayList<>();

        Project project = getAndCheckProject(request.getProjectId());

        String additionalPrompt = request.getAdditionalPrompts();
        String optimizedPrompt = (additionalPrompt != null && !additionalPrompt.isEmpty())
                ? promptOptimizationService.optimizePromptForDiffusion(additionalPrompt)
                : "No further instructions.";

        String promptContent = promptTemplateService.getTemplate(PromptType.IMAGE_CHANGE_ART_STYLE_HF);
        promptContent = promptContent.replace("{NEW_ART_STYLE}", resolveArtStyleHF(request.getTargetStyle()));
        promptContent = promptContent.replace("{PROMPT}", optimizedPrompt);

        byte[] imgBytes = persistenceService.downloadImageFromPersistence(request.getImageInfo().getImagePath());
        if (imgBytes == null || imgBytes.length == 0) {
            return ImageGenerationResponse.builder().imageUrls(pathList).updatedInstruction("N/A").build();
        }
        String mimeType = request.getImageInfo().getMimeType().getValue();
        String dataUri = "data:" + mimeType + ";base64," + Base64.getEncoder().encodeToString(imgBytes);

        byte[] imageBytes = falAIService.editImageQwen(promptContent, List.of(dataUri));

        String outputPath = persistenceService.uploadServerImageToPersistence(imageBytes);
        List<byte[]> imageData = new ArrayList<>();
        if (!outputPath.isEmpty()) {
            imageData.add(imageBytes);
            MediaDTO media = mediaService.addServerMedia(outputPath, MediaType.IMAGE);
            albumService.addMediaToProjectAlbum(media.getId(), MediaType.IMAGE, request.getProjectId());
            pathList.add(persistenceService.getMediaUrl(outputPath));
        }

        return ImageGenerationResponse.builder()
                .imageUrls(pathList)
                .updatedInstruction("N/A")
                .build();
    }


    @Override
    public ImageGenerationResponse generateSplashArtGPT(SplashArtGenerationRequest request) {
        List<String> pathList = new java.util.ArrayList<>();

        Project project = getAndCheckProject(request.getProjectId());
        String context = String.join(";", project.getInstructions());

        String optimizedPrompt = promptOptimizationService.optimizePrompt(request.getSplashDescription());

        String promptContent = promptTemplateService.getTemplate(PromptType.SPLASH_ART_GENERATION);
        promptContent = promptContent.replace("{CONTEXT}", context);
        promptContent = promptContent.replace("{ART_STYLE}", resolveArtStyle(project.getArtStyle()));
        promptContent = promptContent.replace("{SPLASH_ART_DESCRIPTION}", optimizedPrompt);

        byte[] imageBytes = falAIService.generateImageGPT(promptContent);

        String outputPath = persistenceService.uploadServerImageToPersistence(imageBytes);
        List<byte[]> imageData = new ArrayList<>();
        if (!outputPath.isEmpty()) {
            imageData.add(imageBytes);
            MediaDTO media = mediaService.addServerMedia(outputPath, MediaType.IMAGE);
            albumService.addMediaToProjectAlbum(media.getId(), MediaType.IMAGE, request.getProjectId());
            pathList.add(persistenceService.getMediaUrl(outputPath));
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
    public ImageGenerationResponse generateImageVariationGPT(ImageVariationRequest request) {
        List<String> pathList = new java.util.ArrayList<>();

        Project project = getAndCheckProject(request.getProjectId());
        String context = String.join(";", project.getInstructions());

        String optimizedPrompt = promptOptimizationService.optimizePrompt(request.getPrompt());

        String promptContent = promptTemplateService.getTemplate(PromptType.IMAGE_EDIT);
        promptContent = promptContent.replace("{CONTEXT}", context);
        promptContent = promptContent.replace("{ART_STYLE}", resolveArtStyle(project.getArtStyle()));
        promptContent = promptContent.replace("{PROMPT}", optimizedPrompt);

        List<String> imageDataUris = new ArrayList<>();
        if (request.getImageInfos() != null) {
            for (ImageInfo imageInfo : request.getImageInfos()) {
                byte[] imgBytes = persistenceService.downloadImageFromPersistence(imageInfo.getImagePath());
                if (imgBytes != null && imgBytes.length > 0) {
                    String mimeType = imageInfo.getMimeType().getValue();
                    imageDataUris.add("data:" + mimeType + ";base64," + Base64.getEncoder().encodeToString(imgBytes));
                }
            }
        }

        byte[] imageBytes = imageDataUris.isEmpty()
                ? falAIService.generateImageGPT(promptContent)
                : falAIService.editImageGPT(promptContent, imageDataUris);

        String outputPath = persistenceService.uploadServerImageToPersistence(imageBytes);
        List<byte[]> imageData = new ArrayList<>();
        if (!outputPath.isEmpty()) {
            imageData.add(imageBytes);
            MediaDTO media = mediaService.addServerMedia(outputPath, MediaType.IMAGE);
            albumService.addMediaToProjectAlbum(media.getId(), MediaType.IMAGE, request.getProjectId());
            pathList.add(persistenceService.getMediaUrl(outputPath));
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
    public ImageGenerationResponse generateSpriteSheetGPT(SpriteSheetGenerationRequest request) {
        List<String> pathList = new java.util.ArrayList<>();

        Project project = getAndCheckProject(request.getProjectId());
        String context = String.join(";", project.getInstructions());

        String additionalCharacterDescription = request.getCharacterDescription();
        String optimizedCharacterDescription = (additionalCharacterDescription != null && !additionalCharacterDescription.isEmpty())
                ? promptOptimizationService.optimizePrompt(additionalCharacterDescription)
                : "No character description.";

        String additionalActionDescription = request.getActionDescription();
        String optimizedActionDescription = (additionalActionDescription != null && !additionalActionDescription.isEmpty())
                ? promptOptimizationService.optimizePrompt(additionalActionDescription)
                : "No action description.";

        String promptContent = promptTemplateService.getTemplate(PromptType.SPRITE_SHEET_GENERATION);
        promptContent = promptContent.replace("{CONTEXT}", context);
        promptContent = promptContent.replace("{ART_STYLE}", resolveArtStyle(project.getArtStyle()));
        promptContent = promptContent.replace("{CHARACTER_DESCRIPTION}", optimizedCharacterDescription);
        promptContent = promptContent.replace("{CHARACTER_ACTION}", optimizedActionDescription);

        List<String> imageDataUris = new ArrayList<>();
        if (request.getImageInfos() != null) {
            for (ImageInfo imageInfo : request.getImageInfos()) {
                byte[] imgBytes = persistenceService.downloadImageFromPersistence(imageInfo.getImagePath());
                if (imgBytes != null && imgBytes.length > 0) {
                    String mimeType = imageInfo.getMimeType().getValue();
                    imageDataUris.add("data:" + mimeType + ";base64," + Base64.getEncoder().encodeToString(imgBytes));
                }
            }
        }

        byte[] imageBytes = imageDataUris.isEmpty()
                ? falAIService.generateImageGPT(promptContent)
                : falAIService.editImageGPT(promptContent, imageDataUris);

        String outputPath = persistenceService.uploadServerImageToPersistence(imageBytes);
        List<byte[]> imageData = new ArrayList<>();
        if (!outputPath.isEmpty()) {
            imageData.add(imageBytes);
            MediaDTO media = mediaService.addServerMedia(outputPath, MediaType.IMAGE);
            albumService.addMediaToProjectAlbum(media.getId(), MediaType.IMAGE, request.getProjectId());
            pathList.add(persistenceService.getMediaUrl(outputPath));
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
    public ImageGenerationResponse changeImageStyleGPT(ImageStyleChangeRequest request) {
        List<String> pathList = new java.util.ArrayList<>();

        Project project = getAndCheckProject(request.getProjectId());

        String additionalPrompt = request.getAdditionalPrompts();
        String optimizedPrompt = (additionalPrompt != null && !additionalPrompt.isEmpty())
                ? promptOptimizationService.optimizePrompt(additionalPrompt)
                : "No further instructions.";

        String promptContent = promptTemplateService.getTemplate(PromptType.IMAGE_CHANGE_ART_STYLE);
        promptContent = promptContent.replace("{NEW_ART_STYLE}", resolveArtStyle(request.getTargetStyle()));
        promptContent = promptContent.replace("{PROMPT}", optimizedPrompt);

        byte[] imgBytes = persistenceService.downloadImageFromPersistence(request.getImageInfo().getImagePath());
        if (imgBytes == null || imgBytes.length == 0) {
            return ImageGenerationResponse.builder().imageUrls(pathList).updatedInstruction("N/A").build();
        }
        String mimeType = request.getImageInfo().getMimeType().getValue();
        String dataUri = "data:" + mimeType + ";base64," + Base64.getEncoder().encodeToString(imgBytes);

        byte[] imageBytes = falAIService.editImageGPT(promptContent, List.of(dataUri));

        String outputPath = persistenceService.uploadServerImageToPersistence(imageBytes);
        List<byte[]> imageData = new ArrayList<>();
        if (!outputPath.isEmpty()) {
            imageData.add(imageBytes);
            MediaDTO media = mediaService.addServerMedia(outputPath, MediaType.IMAGE);
            albumService.addMediaToProjectAlbum(media.getId(), MediaType.IMAGE, request.getProjectId());
            pathList.add(persistenceService.getMediaUrl(outputPath));
        }

        return ImageGenerationResponse.builder()
                .imageUrls(pathList)
                .updatedInstruction("N/A")
                .build();
    }

    private String resolveArtStyle(ArtStyle artStyle) {
        String template = styleTemplateService.getTemplate(artStyle);
        return (template != null && !template.isEmpty()) ? template : artStyle.name();
    }

    private String resolveArtStyleHF(ArtStyle artStyle) {
        String template = styleTemplateService.getHFTemplate(artStyle);
        return (template != null && !template.isEmpty()) ? template : artStyle.name();
    }

    private Project getAndCheckProject(Long projectId) {
        if (projectId == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "You can't generate images without a project");
        }

        Project project = projectRepository.findById(projectId).orElse(null);

        if (project == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Project doesn't exist");
        }

        User currentUser = userRepository.findByEmail(AuthenticationUtils.getCurrentUserEmail())
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "User not found"));

        if (!project.getUser().equals(currentUser)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "You are not the owner of this project");
        }

        return project;
    }
}
