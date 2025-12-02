package com.Artiom.ArtifexAI.Persistence.Service;

import com.Artiom.ArtifexAI.ImageGeneration.DTO.MimeType;

public interface PersistenceService {
    String uploadServerImageToPersistence(byte[] data);

    String uploadServerVideoToPersistence(byte[] data);

    String uploadClientImageToPersistence(String base64, MimeType mimeType);

    String getMediaUrl(String imagePath);

    byte[] downloadImageFromPersistence(String imagePath);

    void deleteImageFromPersistence(String imagePath);
}
