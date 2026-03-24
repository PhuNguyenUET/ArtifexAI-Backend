package com.Artiom.ArtifexAI.ImageGeneration.DTO;

import com.google.genai.types.EditMode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageEditWithMaskRequest {
    private String projectId;
    private ImageInfo imageInfo;
    private String maskImageBase64;
    private String prompt;
    private EditMode.Known editMode;
}
