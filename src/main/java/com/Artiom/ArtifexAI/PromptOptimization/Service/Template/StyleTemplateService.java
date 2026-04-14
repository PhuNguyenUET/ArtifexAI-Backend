package com.Artiom.ArtifexAI.PromptOptimization.Service.Template;

import com.Artiom.ArtifexAI.Project.Model.ArtStyle;

public interface StyleTemplateService {
    void loadTemplate(ArtStyle artStyle, String fileName);

    String getTemplate(ArtStyle artStyle);
}
