package com.Artiom.ArtifexAI.Media.DTO;

import com.Artiom.ArtifexAI.ImageGeneration.DTO.MimeType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageClientUploadDTO {
    private String base64;
    private MimeType mimeType;
}
