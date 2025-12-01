package com.Artiom.ArtifexAI.Image.Service;

import com.Artiom.ArtifexAI.Image.DTO.AlbumCreateDTO;
import com.Artiom.ArtifexAI.Image.DTO.AlbumDTO;
import com.Artiom.ArtifexAI.Image.DTO.AlbumEditDTO;
import com.Artiom.ArtifexAI.Image.DTO.AlbumImageDTO;

import java.util.List;

public interface AlbumService {
    AlbumDTO createAlbum(AlbumCreateDTO albumCreateDTO);

    void createAlbumForProject(String name, String projectId);

    void deleteAlbum(String albumId);

    void editAlbum(AlbumEditDTO albumEditDTO);

    void addImageToAlbum(AlbumImageDTO albumImageDTO);

    void addImageToProjectAlbum(String imageId, String projectId);

    void removeImageFromAlbum(AlbumImageDTO albumImageDTO);

    void unlinkProjectAlbum(String projectId);

    AlbumDTO getAlbumById(String albumId);

    List<AlbumDTO> getAllAlbums();
}
