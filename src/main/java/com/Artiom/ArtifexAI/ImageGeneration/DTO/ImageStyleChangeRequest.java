package com.Artiom.ArtifexAI.ImageGeneration.DTO;

import com.Artiom.ArtifexAI.Project.Model.ArtStyle;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageStyleChangeRequest {
    private String projectId;
    private ImageInfo imageInfo;
    private ArtStyle targetStyle;
    private String additionalPrompts;
}
