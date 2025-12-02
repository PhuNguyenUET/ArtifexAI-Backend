package com.Artiom.ArtifexAI.Media.Service.Impl;

import com.Artiom.ArtifexAI.Common.Exception.BusinessException;
import com.Artiom.ArtifexAI.Media.DTO.ImageClientUploadDTO;
import com.Artiom.ArtifexAI.Media.DTO.MediaDTO;
import com.Artiom.ArtifexAI.Media.Model.Album;
import com.Artiom.ArtifexAI.Media.Model.Media;
import com.Artiom.ArtifexAI.Media.Model.MediaType;
import com.Artiom.ArtifexAI.Media.Model.PresignedMediaInfo;
import com.Artiom.ArtifexAI.Media.Repository.AlbumRepository;
import com.Artiom.ArtifexAI.Media.Repository.MediaRepository;
import com.Artiom.ArtifexAI.Media.Service.MediaService;
import com.Artiom.ArtifexAI.Persistence.Service.PersistenceService;
import com.Artiom.ArtifexAI.User.Model.User;
import com.Artiom.ArtifexAI.Util.AuthenticationUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {
    @Value("${aws.cloudfront.url-access-time}")
    private int presignedAccessTimeInHours;

    private final MediaRepository mediaRepository;
    private final AlbumRepository albumRepository;
    private final PersistenceService persistenceService;

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

                    if(source.getPresignedMediaInfo().getPresignedUrlExpireTime() < System.currentTimeMillis()) {
                        String presignedUrl = persistenceService.getMediaUrl(source.getMediaPath());
                        source.getPresignedMediaInfo().setMediaPresignedUrl(presignedUrl);
                        source.getPresignedMediaInfo().setPresignedUrlExpireTime(System.currentTimeMillis() + (long) presignedAccessTimeInHours * 3600 * 1000);

                        mediaRepository.save(source);
                    }

                    return MediaDTO.builder()
                            .id(source.getId())
                            .mediaPath(source.getMediaPath())
                            .mediaUrl(source.getPresignedMediaInfo().getMediaPresignedUrl())
                            .createdDate(source.getCreatedDate())
                            .build();
                });
    }

    @Override
    public MediaDTO addServerMedia(String mediaPath, MediaType mediaType) {
        User currentUser = AuthenticationUtils.getCurrentUser();

        Media media = Media.builder()
                .mediaPath(mediaPath)
                .mediaType(mediaType)
                .userId(currentUser.getId())
                .build();

        Media savedMedia = mediaRepository.save(media);
        return modelMapper.map(savedMedia, MediaDTO.class);
    }

    @Override
    public MediaDTO addClientImage(ImageClientUploadDTO imageClientUploadDTO) {
        User currentUser = AuthenticationUtils.getCurrentUser();

        String imagePath = persistenceService.uploadClientImageToPersistence(imageClientUploadDTO.getBase64(), imageClientUploadDTO.getMimeType());
        Media media = Media.builder()
                .mediaPath(imagePath)
                .mediaType(MediaType.IMAGE)
                .userId(currentUser.getId())
                .build();

        Media savedMedia = mediaRepository.save(media);
        return modelMapper.map(savedMedia, MediaDTO.class);
    }

    @Override
    public void deleteMedia(String mediaId) {
        Media media = getAndCheckImage(mediaId);

        persistenceService.deleteImageFromPersistence(media.getMediaPath());
        mediaRepository.delete(media);
    }

    @Override
    public MediaDTO getMediaById(String mediaId) {
        Media media = getAndCheckImage(mediaId);

        return modelMapper.map(media, MediaDTO.class);
    }

    @Override
    public List<MediaDTO> getGallery() {
        User currentUser = AuthenticationUtils.getCurrentUser();

        return mediaRepository.findAllByUserId(currentUser.getId()).stream().map(media -> modelMapper.map(media, MediaDTO.class)).collect(Collectors.toList());
    }

    @Override
    public List<MediaDTO> getMediasByAlbum(String albumId) {
        User currentUser = AuthenticationUtils.getCurrentUser();

        Album album = albumRepository.findById(albumId).orElse(null);
        if(album == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Album doesn't exist");
        }

        if(!album.getUserId().equals(currentUser.getId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "You are not the owner of this album");
        }

        List<Media> mediaList = mediaRepository.findAllById(album.getImages());

        boolean changedContent = false;

        List<String> imageIds = album.getImages();
        for (String imageId : imageIds) {
            Media media = mediaRepository.findById(imageId).orElse(null);
            if (media != null && media.getUserId().equals(currentUser.getId())) {
                mediaList.add(media);
            } else {
                album.getImages().remove(imageId);
                changedContent = true;
            }
        }

        if(changedContent) {
            albumRepository.save(album);
        }

        return mediaList.stream().map(media -> modelMapper.map(media, MediaDTO.class)).collect(Collectors.toList());
    }

    private Media getAndCheckImage(String imageId) {
        User currentUser = AuthenticationUtils.getCurrentUser();

        Media media = mediaRepository.findById(imageId).orElse(null);
        if(media == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Image doesn't exist");
        }

        if(!media.getUserId().equals(currentUser.getId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "You are not the owner of this image");
        }
        return media;
    }
}
