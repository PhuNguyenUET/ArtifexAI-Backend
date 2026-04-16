package com.Artiom.ArtifexAI.HuggingFace;

import java.util.List;

public interface HuggingFaceService {
    byte[] generateImage(String prompt);

    byte[] editImage(String prompt, List<String> imageDataUris);

    byte[] generateImageQwen(String prompt);

    /**
     * Edit an image using the Qwen image-edit-plus model.
     *
     * @param prompt        the editing prompt
     * @param imageDataUris list of base64 data-URIs to use as references
     * @return raw PNG bytes of the generated image
     */
    byte[] editImageQwen(String prompt, List<String> imageDataUris);
}
