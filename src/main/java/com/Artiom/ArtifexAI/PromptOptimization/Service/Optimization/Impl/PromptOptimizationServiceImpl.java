 package com.Artiom.ArtifexAI.PromptOptimization.Service.Optimization.Impl;

 import com.Artiom.ArtifexAI.PromptOptimization.Model.PromptType;
 import com.Artiom.ArtifexAI.PromptOptimization.Service.Optimization.PromptOptimizationService;
 import com.Artiom.ArtifexAI.PromptOptimization.Service.Template.PromptTemplateService;
 import com.google.common.collect.ImmutableList;
 import com.google.genai.Client;
 import com.google.genai.types.*;
 import jakarta.annotation.PostConstruct;
 import lombok.RequiredArgsConstructor;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Service;

 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.stream.Collectors;

 @Service
@RequiredArgsConstructor
public class PromptOptimizationServiceImpl implements PromptOptimizationService {
    @Value("${gemini.textModel}")
    private String modelName;

    private ImmutableList<SafetySetting> safetySettings;

    private final Client client;
    private final PromptTemplateService promptTemplateService;

    @PostConstruct
    private void buildSafetySettings() {
        safetySettings = ImmutableList.of(
                SafetySetting.builder()
                        .category(HarmCategory.Known.HARM_CATEGORY_HATE_SPEECH)
                        .threshold(HarmBlockThreshold.Known.BLOCK_ONLY_HIGH)
                        .build(),
                SafetySetting.builder()
                        .category(HarmCategory.Known.HARM_CATEGORY_DANGEROUS_CONTENT)
                        .threshold(HarmBlockThreshold.Known.BLOCK_ONLY_HIGH)
                        .build(),
                SafetySetting.builder()
                        .category(HarmCategory.Known.HARM_CATEGORY_HARASSMENT)
                        .threshold(HarmBlockThreshold.Known.BLOCK_ONLY_HIGH)
                        .build(),
                SafetySetting.builder()
                        .category(HarmCategory.Known.HARM_CATEGORY_SEXUALLY_EXPLICIT)
                        .threshold(HarmBlockThreshold.Known.BLOCK_ONLY_HIGH)
                        .build(),
                SafetySetting.builder()
                        .category(HarmCategory.Known.HARM_CATEGORY_CIVIC_INTEGRITY)
                        .threshold(HarmBlockThreshold.Known.BLOCK_ONLY_HIGH)
                        .build());
    }

    private Content getSystemInstruction() {
        Content systemInstruction = Content.builder()
                .parts(Part.fromText("You are a professional prompt engineer trying to optimize user's prompt to achieve the best result when calling LLM."))
                .build();

        return systemInstruction;
    }

    @Override
    public String optimizePrompt(String prompt) {
        GenerateContentConfig contentConfig = GenerateContentConfig.builder()
                .responseModalities("TEXT")
                .candidateCount(1)
                .safetySettings(safetySettings)
                .systemInstruction(getSystemInstruction())
                .build();

        String promptContent = promptTemplateService.getTemplate(PromptType.PROMPT_OPTIMIZATION);
        promptContent = promptContent.replace("{PROMPT}", prompt);

        GenerateContentResponse response = client.models.generateContent(
                modelName,
                promptContent,
                contentConfig
        );

        return response.text();
    }

    @Override
    public String optimizePromptForDiffusion(String prompt) {
        GenerateContentConfig contentConfig = GenerateContentConfig.builder()
                .responseModalities("TEXT")
                .candidateCount(1)
                .safetySettings(safetySettings)
                .systemInstruction(getSystemInstruction())
                .build();

        String promptContent = promptTemplateService.getTemplate(PromptType.PROMPT_OPTIMIZATION_HF);
        promptContent = promptContent.replace("{PROMPT}", prompt);

        GenerateContentResponse response = client.models.generateContent(
                modelName,
                promptContent,
                contentConfig
        );

        return response.text();
    }

    @Override
    public String optimizeContextForDiffusion(String prompt, String context) {
        GenerateContentConfig contentConfig = GenerateContentConfig.builder()
                .responseModalities("TEXT")
                .candidateCount(1)
                .safetySettings(safetySettings)
                .systemInstruction(getSystemInstruction())
                .build();

        String promptContent = promptTemplateService.getTemplate(PromptType.CONTEXT_OPTIMIZATION_HF);
        promptContent = promptContent.replace("{PROMPT}", prompt);
        promptContent = promptContent.replace("{CONTEXT}", context);

        GenerateContentResponse response = client.models.generateContent(
                modelName,
                promptContent,
                contentConfig
        );

        return response.text();
    }

    @Override
    public List<String> optimizeInstruction(String instruction) {
        GenerateContentConfig contentConfig = GenerateContentConfig.builder()
                .responseModalities("TEXT")
                .candidateCount(1)
                .safetySettings(safetySettings)
                .systemInstruction(getSystemInstruction())
                .build();

        String promptContent = promptTemplateService.getTemplate(PromptType.INSTRUCTION_OPTIMIZATION);
        promptContent = promptContent.replace("{PROMPT}", instruction);

        GenerateContentResponse response = client.models.generateContent(
                modelName,
                promptContent,
                contentConfig
        );

        String optimizedInstructions = response.text();

        if(optimizedInstructions == null || optimizedInstructions.isEmpty()) {
            return new ArrayList<>();
        }

        return Arrays.stream(optimizedInstructions.split(";"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    @Override
    public String analyzePromptAndImages(String prompt, List<byte[]> imageData, List<String> instructions) {
        String context = String.join(";", instructions);

        GenerateContentConfig contentConfig = GenerateContentConfig.builder()
                .responseModalities("TEXT")
                .candidateCount(1)
                .safetySettings(safetySettings)
                .systemInstruction(getSystemInstruction())
                .build();

        String promptContent = promptTemplateService.getTemplate(PromptType.INSTRUCTION_UPDATE);
        promptContent = promptContent.replace("{CONTEXT}", context);
        promptContent = promptContent.replace("{PROMPT}", prompt);

        List<Part> parts = new ArrayList<>();
        parts.add(Part.fromText(promptContent));

        for (byte[] image : imageData) {
            parts.add(Part.fromBytes(image, "image/png"));
        }

        Content content = Content.fromParts(parts.toArray(new Part[0]));

        GenerateContentResponse response = client.models.generateContent(
                modelName,
                content,
                contentConfig
        );

        return response.text();
    }
}
