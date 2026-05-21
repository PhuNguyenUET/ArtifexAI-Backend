package com.Artiom.ArtifexAI.Media.Service.Impl;

import com.Artiom.ArtifexAI.Common.Exception.BusinessException;
import com.Artiom.ArtifexAI.Media.DTO.ImageClientUploadDTO;
import com.Artiom.ArtifexAI.Media.DTO.MediaDTO;
import com.Artiom.ArtifexAI.Media.Model.*;
import com.Artiom.ArtifexAI.Media.Repository.AlbumMediaRepository;
import com.Artiom.ArtifexAI.Media.Repository.AlbumRepository;
import com.Artiom.ArtifexAI.Media.Repository.MediaRepository;
import com.Artiom.ArtifexAI.Media.Service.MediaService;
import com.Artiom.ArtifexAI.Persistence.Service.PersistenceService;
import com.Artiom.ArtifexAI.Project.Model.Project;
import com.Artiom.ArtifexAI.Project.Repository.ProjectRepository;
import com.Artiom.ArtifexAI.User.Model.User;
import com.Artiom.ArtifexAI.User.Repository.UserRepository;
import com.Artiom.ArtifexAI.Util.AuthenticationUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {
    @Value("${aws.cloudfront.url-access-time}")
    private int presignedAccessTimeInHours;

    private final AlbumMediaRepository albumMediaRepository;
    private final MediaRepository mediaRepository;
    private final AlbumRepository albumRepository;
    private final ProjectRepository projectRepository;
    private final PersistenceService persistenceService;
    private final UserRepository userRepository;

    private ModelMapper modelMapper;

    @PostConstruct
    private void setupModelMapper() {
        modelMapper = new ModelMapper();

        modelMapper.createTypeMap(Media.class, MediaDTO.class)
                .setConverter(context -> {
                    Media source = context.getSource();

                    if(source.getPresignedMediaInfo() == null) {
                        source.setPresignedMediaInfo(new PresignedMediaInfo());
                    }

                    return MediaDTO.builder()
                            .id(source.getId())
                            .mediaPath(source.getMediaPath())
                            .mediaUrl(source.getPresignedMediaInfo().getMediaPresignedUrl())
                            .createdDate(source.getCreatedDate())
                            .build();
                });
    }

    private void refreshExpiredUrls(List<Media> medias) {
        List<Media> expired = medias.stream()
                .filter(m -> {
                    if (m.getPresignedMediaInfo() == null) {
                        m.setPresignedMediaInfo(new PresignedMediaInfo());
                    }
                    return m.getPresignedMediaInfo().getPresignedUrlExpireTime() < System.currentTimeMillis();
                })
                .collect(Collectors.toList());

        if (expired.isEmpty()) return;

        expired.sort(java.util.Comparator.comparing(Media::getId));

        for (Media media : expired) {
            String presignedUrl = persistenceService.getMediaUrl(media.getMediaPath());
            media.getPresignedMediaInfo().setMediaPresignedUrl(presignedUrl);
            media.getPresignedMediaInfo().setPresignedUrlExpireTime(
                    System.currentTimeMillis() + (long) presignedAccessTimeInHours * 3600 * 1000);
        }

        mediaRepository.saveAll(expired);
    }

    @Override
    @Transactional
    public MediaDTO addServerMedia(String mediaPath, MediaType mediaType) {
        User currentUser = getCurrentUser();

        Media media = Media.builder()
                .mediaPath(mediaPath)
                .mediaType(mediaType)
                .user(currentUser)
                .build();

        Media savedMedia = mediaRepository.save(media);
        return modelMapper.map(savedMedia, MediaDTO.class);
    }

    @Override
    @Transactional
    public MediaDTO addClientImage(ImageClientUploadDTO imageClientUploadDTO) {
        User currentUser = getCurrentUser();
        String imagePath = persistenceService.uploadClientImageToPersistence(imageClientUploadDTO.getBase64(), imageClientUploadDTO.getMimeType());
        Media media = Media.builder()
                .mediaPath(imagePath)
                .mediaType(MediaType.IMAGE)
                .user(currentUser)
                .build();

        Media savedMedia = mediaRepository.save(media);
        return modelMapper.map(savedMedia, MediaDTO.class);
    }

    @Override
    @Transactional
    public void deleteMedia(Long mediaId) {
        Media media = getAndCheckMedia(mediaId);

        for (AlbumMedia relation : List.copyOf(media.getAlbumMedias())) {
            relation.getAlbum().getAlbumMedias().remove(relation);
            albumMediaRepository.delete(relation);
        }
        media.getAlbumMedias().clear();

        persistenceService.deleteImageFromPersistence(media.getMediaPath());

        mediaRepository.delete(media);
    }

    @Override
    public MediaDTO getMediaById(Long mediaId) {
        Media media = getAndCheckMedia(mediaId);
        refreshExpiredUrls(List.of(media));
        return modelMapper.map(media, MediaDTO.class);
    }

    @Override
    public List<MediaDTO> getGallery() {
        User currentUser = getCurrentUser();
        List<Media> medias = mediaRepository.findByUser(currentUser);
        refreshExpiredUrls(medias);
        return medias.stream()
                .map(media -> modelMapper.map(media, MediaDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<MediaDTO> getMediasByAlbum(Long albumId) {
        Album album = getAndCheckAlbum(albumId);

        List<Media> medias = album.getAlbumMedias().stream().map(AlbumMedia::getMedia).collect(Collectors.toList());
        refreshExpiredUrls(medias);
        return medias.stream().map(media -> modelMapper.map(media, MediaDTO.class)).collect(Collectors.toList());
    }

    private Media getAndCheckMedia(Long mediaId) {
        Media media = mediaRepository.findById(mediaId).orElse(null);
        if(media == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Image doesn't exist");
        }

        if(!media.getUser().equals(getCurrentUser())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "You are not the owner of this image");
        }
        return media;
    }

    private Project getAndCheckProject(Long projectId) {
        Project project = projectRepository.findById(projectId).orElse(null);
        if(project == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Project doesn't exist");
        }

        if(!project.getUser().equals(getCurrentUser())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "You are not the owner of this project");
        }

        return project;
    }

    private Album getAndCheckAlbum(Long albumId) {
        Album album = albumRepository.findById(albumId).orElse(null);
        if(album == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Album doesn't exist");
        }

        if(!album.getUser().equals(getCurrentUser())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "You are not the owner of this album");
        }

        return album;
    }

    private User getCurrentUser() {
        return userRepository.findByEmail(AuthenticationUtils.getCurrentUserEmail())
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "User not found"));
    }
}
