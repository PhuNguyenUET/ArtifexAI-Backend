package com.Artiom.ArtifexAI.ImageGeneration.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageEditRequest {
    private String projectId;
    private List<ImageInfo> imageInfos;
    private String prompt;
    private int numberOfOutputs;
}
