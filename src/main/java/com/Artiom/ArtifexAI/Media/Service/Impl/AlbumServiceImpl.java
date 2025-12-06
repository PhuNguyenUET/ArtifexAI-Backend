package com.Artiom.ArtifexAI.Media.Service.Impl;

import com.Artiom.ArtifexAI.Common.Exception.BusinessException;
import com.Artiom.ArtifexAI.Media.DTO.*;
import com.Artiom.ArtifexAI.Media.Model.Album;
import com.Artiom.ArtifexAI.Media.Model.Media;
import com.Artiom.ArtifexAI.Media.Model.MediaType;
import com.Artiom.ArtifexAI.Media.Model.PresignedMediaInfo;
import com.Artiom.ArtifexAI.Media.Repository.AlbumRepository;
import com.Artiom.ArtifexAI.Media.Repository.MediaRepository;
import com.Artiom.ArtifexAI.Media.Service.AlbumService;
import com.Artiom.ArtifexAI.Persistence.Service.PersistenceService;
import com.Artiom.ArtifexAI.User.Model.User;
import com.Artiom.ArtifexAI.Util.AuthenticationUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AlbumServiceImpl implements AlbumService {
    @Value("${aws.cloudfront.url-access-time}")
    private int presignedAccessTimeInHours;

    private final AlbumRepository albumRepository;
    private final MediaRepository mediaRepository;
    private final PersistenceService persistenceService;

    private ModelMapper modelMapper;

    @PostConstruct
    private void setupModelMapper() {
        modelMapper = new ModelMapper();

        modelMapper.createTypeMap(Album.class, AlbumDTO.class)
                .setConverter(context -> {
                    Album album = context.getSource();

                    AlbumDTO albumDTO = AlbumDTO.builder()
                            .id(album.getId())
                            .name(album.getName())
                            .createdDate(album.getCreatedDate())
                            .modifiedDate(album.getModifiedDate())
                            .build();

                    List<Media> mediaList = album.getImages().stream().map(imageId -> mediaRepository.findById(imageId).orElse(null)).toList();

                    List<MediaDTO> mediaDTOS = mediaList.stream().map(media -> {
                        if(media == null) {
                            return null;
                        }

                        if(media.getPresignedMediaInfo() == null) {
                            media.setPresignedMediaInfo(new PresignedMediaInfo());
                        }

                        if(media.getPresignedMediaInfo().getPresignedUrlExpireTime() < System.currentTimeMillis()) {
                            String presignedUrl = persistenceService.getMediaUrl(media.getMediaPath());
                            media.getPresignedMediaInfo().setMediaPresignedUrl(presignedUrl);
                            media.getPresignedMediaInfo().setPresignedUrlExpireTime(System.currentTimeMillis() + (long) presignedAccessTimeInHours * 3600 * 1000);

                            mediaRepository.save(media);
                        }

                        return MediaDTO.builder()
                                .id(media.getId())
                                .mediaPath(media.getMediaPath())
                                .mediaUrl(media.getPresignedMediaInfo().getMediaPresignedUrl())
                                .createdDate(media.getCreatedDate())
                                .build();
                    }).toList();

                    mediaDTOS = mediaDTOS.stream().filter(Objects::nonNull).toList();

                    albumDTO.setMediaList(mediaDTOS);
                    return albumDTO;
                });
    }

    @Override
    public AlbumDTO createAlbum(AlbumCreateDTO albumCreateDTO) {
        Album album = Album.builder()
                .name(albumCreateDTO.getName())
                .userId(AuthenticationUtils.getCurrentUser().getId())
                .build();

        List<String> validImageIds = new ArrayList<>();

        for (String songId : albumCreateDTO.getMediaIds()) {
            mediaRepository.findById(songId).ifPresent(media -> validImageIds.add(media.getId()));
        }

        album.setImages(validImageIds);

        Album savedAlbum = albumRepository.save(album);
        return modelMapper.map(savedAlbum, AlbumDTO.class);
    }

    @Override
    public void createAlbumForProject(String name, String projectId) {
        Album album = Album.builder()
                .name(name)
                .userId(AuthenticationUtils.getCurrentUser().getId())
                .projectId(projectId)
                .build();

        albumRepository.save(album);
    }

    @Override
    public void deleteAlbum(String albumId) {
        Album album = getAndCheckAlbum(albumId);

        if(!album.getProjectId().isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Album is linked to a project and cannot be deleted");
        }
        
        albumRepository.delete(album);
    }

    @Override
    public void addMediaToAlbum(AlbumMediaDTO albumMediaDTO) {
        Media media = getAndCheckImage(albumMediaDTO.getMediaId());
        Album album = getAndCheckAlbum(albumMediaDTO.getAlbumId());

        if(!album.getProjectId().isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Album is linked to a project and cannot be edited");
        }

        album.getImages().add(media.getId());
        albumRepository.save(album);
    }

    @Override
    public void addMediaToProjectAlbum(String mediaId, MediaType mediaType, String projectId) {
        Media media = getAndCheckImage(mediaId);

        Album album = albumRepository.findByProjectId(projectId).orElse(null);
        if(album == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Album for the specified project doesn't exist");
        }

        if(mediaType == MediaType.IMAGE) {
            album.getImages().add(media.getId());
        } else {
            album.getVideos().add(media.getId());
        }

        albumRepository.save(album);
    }

    @Override
    public void editAlbum(AlbumEditDTO albumEditDTO) {
        Album album = getAndCheckAlbum(albumEditDTO.getAlbumId());

        if(!album.getProjectId().isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Album is linked to a project and cannot be edited");
        }

        if(albumEditDTO.getName() != null && !albumEditDTO.getName().isEmpty()) {
            album.setName(albumEditDTO.getName());
        }

        albumRepository.save(album);
    }

    @Override
    public void removeMediaFromAlbum(AlbumMediaDTO albumMediaDTO) {
        getAndCheckImage(albumMediaDTO.getMediaId());
        Album album = getAndCheckAlbum(albumMediaDTO.getAlbumId());

        if(!album.getProjectId().isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Album is linked to a project and cannot be deleted");
        }

        album.getImages().remove(albumMediaDTO.getMediaId());
        albumRepository.save(album);
    }

    @Override
    public void unlinkProjectAlbum(String projectId) {
        Album album = albumRepository.findByProjectId(projectId).orElse(null);
        if(album == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Album for the specified project doesn't exist");
        }

        album.setProjectId("");
        albumRepository.save(album);
    }

    @Override
    public AlbumDTO getAlbumById(String albumId) {
        Album album = getAndCheckAlbum(albumId);

        return modelMapper.map(album, AlbumDTO.class);
    }

    @Override
    public List<AlbumDTO> getAllAlbums() {
        User currentUser = AuthenticationUtils.getCurrentUser();

        List<Album> albums = albumRepository.findAllByUserId(currentUser.getId());
        return albums.stream().map(album -> modelMapper.map(album, AlbumDTO.class)).toList();
    }

    private Album getAndCheckAlbum(String albumId) {
        Album album = albumRepository.findById(albumId).orElse(null);

        if(album == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Album doesn't exist");
        }

        if(!album.getUserId().equals(AuthenticationUtils.getCurrentUser().getId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "You are not the owner of this album");
        }
        
        return album;
    }

    private Media getAndCheckImage(String imageId) {
        Media media = mediaRepository.findById(imageId).orElse(null);

        if(media == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Image doesn't exist");
        }

        if (!media.getUserId().equals(AuthenticationUtils.getCurrentUser().getId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "You are not the owner of this image");
        }
        return media;
    }
}
