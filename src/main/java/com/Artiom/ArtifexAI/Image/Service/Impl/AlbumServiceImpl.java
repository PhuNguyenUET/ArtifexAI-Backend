package com.Artiom.ArtifexAI.Image.Service.Impl;

import com.Artiom.ArtifexAI.Common.Exception.BusinessException;
import com.Artiom.ArtifexAI.Image.DTO.*;
import com.Artiom.ArtifexAI.Image.Model.Album;
import com.Artiom.ArtifexAI.Image.Model.Image;
import com.Artiom.ArtifexAI.Image.Repository.AlbumRepository;
import com.Artiom.ArtifexAI.Image.Repository.ImageRepository;
import com.Artiom.ArtifexAI.Image.Service.AlbumService;
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
    private final ImageRepository imageRepository;
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

                    List<Image> images = album.getImages().stream().map(imageId -> imageRepository.findById(imageId).orElse(null)).toList();

                    List<ImageDTO> imageDTOS = images.stream().map(image -> {
                        if(image == null) {
                            return null;
                        }

                        if(image.getPresignedImageInfo().getPresignedUrlExpireTime() < System.currentTimeMillis()) {
                            String presignedUrl = persistenceService.getImageUrl(image.getImagePath());
                            image.getPresignedImageInfo().setImagePresignedUrl(presignedUrl);
                            image.getPresignedImageInfo().setPresignedUrlExpireTime(System.currentTimeMillis() + (long) presignedAccessTimeInHours * 3600 * 1000);

                            imageRepository.save(image);
                        }

                        return ImageDTO.builder()
                                .id(image.getId())
                                .imageUrl(image.getPresignedImageInfo().getImagePresignedUrl())
                                .createdDate(image.getCreatedDate())
                                .build();
                    }).toList();

                    imageDTOS = imageDTOS.stream().filter(Objects::nonNull).toList();

                    albumDTO.setImages(imageDTOS);
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

        for (String songId : albumCreateDTO.getImageIds()) {
            imageRepository.findById(songId).ifPresent(image -> validImageIds.add(image.getId()));
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
    public void addImageToAlbum(AlbumImageDTO albumImageDTO) {
        Image image = getAndCheckImage(albumImageDTO.getImageId());
        Album album = getAndCheckAlbum(albumImageDTO.getAlbumId());

        if(!album.getProjectId().isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Album is linked to a project and cannot be edited");
        }

        album.getImages().add(image.getId());
        albumRepository.save(album);
    }

    @Override
    public void addImageToProjectAlbum(String imageId, String projectId) {
        Image image = getAndCheckImage(imageId);

        Album album = albumRepository.findByProjectId(projectId).orElse(null);
        if(album == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Album for the specified project doesn't exist");
        }

        album.getImages().add(image.getId());
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
    public void removeImageFromAlbum(AlbumImageDTO albumImageDTO) {
        getAndCheckImage(albumImageDTO.getImageId());
        Album album = getAndCheckAlbum(albumImageDTO.getAlbumId());

        if(!album.getProjectId().isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Album is linked to a project and cannot be deleted");
        }

        album.getImages().remove(albumImageDTO.getImageId());
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

    private Image getAndCheckImage(String imageId) {
        Image image = imageRepository.findById(imageId).orElse(null);

        if(image == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Image doesn't exist");
        }

        if (!image.getUserId().equals(AuthenticationUtils.getCurrentUser().getId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "You are not the owner of this image");
        }
        return image;
    }
}
