package com.Artiom.ArtifexAI.PromptOptimization.Service.Template.Impl;

import com.Artiom.ArtifexAI.Project.Model.ArtStyle;
import com.Artiom.ArtifexAI.PromptOptimization.Service.Template.StyleTemplateService;
import com.Artiom.ArtifexAI.Util.FileUtils;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class StyleTemplateServiceImpl implements StyleTemplateService {
    private final Map<ArtStyle, String> templates = new HashMap<>();

    @PostConstruct
    private void init() {
        loadTemplate(ArtStyle.PIXELATED, "pixelated.txt");
        loadTemplate(ArtStyle.HAND_DRAWN, "hand-drawn.txt");
        loadTemplate(ArtStyle.MINIMALIST, "minimalist.txt");
        loadTemplate(ArtStyle.ANIME, "anime.txt");
        loadTemplate(ArtStyle.CARTOON, "cartoon.txt");
        loadTemplate(ArtStyle.REALISTIC, "realistic.txt");
        loadTemplate(ArtStyle.HYPER_REALISTIC, "hyper-realistic.txt");
    }

    @Override
    public void loadTemplate(ArtStyle artStyle, String fileName) {
        try {
            String templatePath = System.getProperty("user.dir") + File.separator + "style_template" + File.separator + fileName;

            templates.put(artStyle, FileUtils.readFromFile(templatePath));
        } catch (Exception e) {
            log.error("Error loading mail template", e);
        }
    }

    @Override
    public String getTemplate(ArtStyle artStyle) {
        return templates.get(artStyle);
    }
}
