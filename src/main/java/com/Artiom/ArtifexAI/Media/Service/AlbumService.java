package com.Artiom.ArtifexAI.Media.Service;

import com.Artiom.ArtifexAI.Media.DTO.AlbumCreateDTO;
import com.Artiom.ArtifexAI.Media.DTO.AlbumDTO;
import com.Artiom.ArtifexAI.Media.DTO.AlbumEditDTO;
import com.Artiom.ArtifexAI.Media.DTO.AlbumMediaDTO;
import com.Artiom.ArtifexAI.Media.Model.MediaType;

import java.util.List;

public interface AlbumService {
    AlbumDTO createAlbum(AlbumCreateDTO albumCreateDTO);

    void createAlbumForProject(String name, String projectId);

    void deleteAlbum(String albumId);

    void editAlbum(AlbumEditDTO albumEditDTO);

    void addMediaToAlbum(AlbumMediaDTO albumMediaDTO);

    void addMediaToProjectAlbum(String mediaId, MediaType mediaType, String projectId);

    void removeMediaFromAlbum(AlbumMediaDTO albumMediaDTO);

    void unlinkProjectAlbum(String projectId);

    AlbumDTO getAlbumById(String albumId);

    List<AlbumDTO> getAllAlbums();
}
