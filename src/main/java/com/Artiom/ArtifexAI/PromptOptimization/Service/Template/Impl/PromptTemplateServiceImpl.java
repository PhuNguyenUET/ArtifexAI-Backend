package com.Artiom.ArtifexAI.PromptOptimization.Service.Template.Impl;

import com.Artiom.ArtifexAI.PromptOptimization.Model.PromptType;
import com.Artiom.ArtifexAI.PromptOptimization.Service.Template.PromptTemplateService;
import com.Artiom.ArtifexAI.Util.FileUtils;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Service
public class PromptTemplateServiceImpl implements PromptTemplateService {
    private final Map<PromptType, String> templates = new HashMap<>();

    @PostConstruct
    private void init() {
        loadTemplate(PromptType.PROMPT_OPTIMIZATION, "prompt-optimization.txt");
        loadTemplate(PromptType.CONTEXT_OPTIMIZATION_HF, "context-optimization-hf.txt");
        loadTemplate(PromptType.INSTRUCTION_OPTIMIZATION, "instruction-optimization.txt");
        loadTemplate(PromptType.INSTRUCTION_UPDATE, "instruction-update.txt");
        loadTemplate(PromptType.IMAGE_CHANGE_ART_STYLE, "image-change-art-style.txt");
        loadTemplate(PromptType.SPLASH_ART_GENERATION, "splash-art-generation.txt");
        loadTemplate(PromptType.SPRITE_SHEET_GENERATION, "sprite-sheet-generation.txt");
        loadTemplate(PromptType.IMAGE_EDIT, "image-edit.txt");
        loadTemplate(PromptType.IMAGE_MASKED_EDIT, "image-masked-edit.txt");
        loadTemplate(PromptType.VIDEO_GENERATION, "video-generation.txt");
        loadTemplate(PromptType.IMAGE_EDIT_HF, "image-edit-hf.txt");
        loadTemplate(PromptType.IMAGE_CHANGE_ART_STYLE_HF, "image-change-art-style-hf.txt");
        loadTemplate(PromptType.SPRITE_SHEET_GENERATION_HF, "sprite-sheet-generation-hf.txt");
        loadTemplate(PromptType.PROMPT_OPTIMIZATION_HF, "prompt-optimization-hf.txt");
        loadTemplate(PromptType.SPLASH_ART_GENERATION_HF, "splash-art-generation-hf.txt");
    }

    @Override
    public void loadTemplate(PromptType promptType, String fileName) {
        try {
            String templatePath = System.getProperty("user.dir") + File.separator + "prompt_template" + File.separator + fileName;

            templates.put(promptType, FileUtils.readFromFile(templatePath));
        } catch (Exception ignored) {
        }
    }

    @Override
    public String getTemplate(PromptType promptType) {
        return templates.get(promptType);
    }
}
