package com.Artiom.ArtifexAI.PromptOptimization.Service.Template;

import com.Artiom.ArtifexAI.PromptOptimization.Model.PromptType;

public interface PromptTemplateService {
    void loadTemplate(PromptType promptType, String fileName);

    String getTemplate(PromptType promptType);
}
