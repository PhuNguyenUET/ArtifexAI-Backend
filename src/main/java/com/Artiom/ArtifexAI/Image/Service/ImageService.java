package com.Artiom.ArtifexAI.Image.Service;

import com.Artiom.ArtifexAI.Image.DTO.ImageDTO;

import java.util.List;

public interface ImageService {
    ImageDTO addImage(String imagePath);

    void deleteImage(String imageId);

    ImageDTO getImageById(String imageId);

    List<ImageDTO> getGallery();

    List<ImageDTO> getImagesByAlbum(String albumId);
}
