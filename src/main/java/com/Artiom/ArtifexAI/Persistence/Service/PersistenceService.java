package com.Artiom.ArtifexAI.Persistence.Service;

public interface PersistenceService {
    String uploadImageToPersistence(byte[] data);

    String getImageUrl(String imagePath);

    byte[] downloadImageFromPersistence(String imagePath);

    void deleteImageFromPersistence(String imagePath);
}
