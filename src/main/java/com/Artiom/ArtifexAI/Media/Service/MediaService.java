package com.Artiom.ArtifexAI.Media.Service;

import com.Artiom.ArtifexAI.Media.DTO.ImageClientUploadDTO;
import com.Artiom.ArtifexAI.Media.DTO.MediaDTO;
import com.Artiom.ArtifexAI.Media.Model.MediaType;

import java.util.List;

public interface MediaService {
    MediaDTO addServerMedia(String mediaPath, MediaType mediaType);

    MediaDTO addClientImage(ImageClientUploadDTO imageClientUploadDTO);

    void deleteMedia(String mediaId);

    MediaDTO getMediaById(String mediaId);

    List<MediaDTO> getGallery();

    List<MediaDTO> getMediasByAlbum(String albumId);
}
