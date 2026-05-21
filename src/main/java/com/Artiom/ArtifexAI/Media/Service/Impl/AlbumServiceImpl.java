package com.Artiom.ArtifexAI.Media.Service.Impl;

import com.Artiom.ArtifexAI.Common.Exception.BusinessException;
import com.Artiom.ArtifexAI.Media.DTO.*;
import com.Artiom.ArtifexAI.Media.Model.*;
import com.Artiom.ArtifexAI.Media.Repository.AlbumMediaRepository;
import com.Artiom.ArtifexAI.Media.Repository.AlbumRepository;
import com.Artiom.ArtifexAI.Media.Repository.MediaRepository;
import com.Artiom.ArtifexAI.Media.Service.AlbumService;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AlbumServiceImpl implements AlbumService {
    @Value("${aws.cloudfront.url-access-time}")
    private int presignedAccessTimeInHours;

    private final AlbumMediaRepository albumMediaRepository;
    private final AlbumRepository albumRepository;
    private final MediaRepository mediaRepository;
    private final ProjectRepository projectRepository;
    private final PersistenceService persistenceService;
    private final UserRepository userRepository;

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

                    List<Media> mediaList = album.getAlbumMedias().stream().map(AlbumMedia::getMedia).toList();

                    List<MediaDTO> mediaDTOS = mediaList.stream().map(media -> {
                        if (media == null) {
                            return null;
                        }

                        return MediaDTO.builder()
                                .id(media.getId())
                                .mediaPath(media.getMediaPath())
                                .mediaUrl(media.getPresignedMediaInfo() != null ? media.getPresignedMediaInfo().getMediaPresignedUrl() : null)
                                .createdDate(media.getCreatedDate())
                                .build();
                    }).filter(Objects::nonNull).toList();

                    albumDTO.setMediaList(mediaDTOS);
                    return albumDTO;
                });
    }

    @Override
    @Transactional
    public AlbumDTO createAlbum(AlbumCreateDTO albumCreateDTO) {
        User currentUser = getCurrentUser();

        Album album = Album.builder()
                .name(albumCreateDTO.getName())
                .user(currentUser)
                .build();

        for (Long mediaId : albumCreateDTO.getMediaIds()) {
            mediaRepository.findById(mediaId)
                    .ifPresent(album::addMedia);
        }

        Album savedAlbum = albumRepository.save(album);

        return modelMapper.map(savedAlbum, AlbumDTO.class);
    }

    @Override
    @Transactional
    public void createAlbumForProject(String name, Long projectId) {
        User currentUser = getCurrentUser();
        Project project = getAndCheckProject(projectId);

        Album album = Album.builder()
                .name(name)
                .user(currentUser)
                .build();

        Album savedAlbum = albumRepository.save(album);

        project.setAlbum(savedAlbum);
        projectRepository.save(project);
    }

    @Override
    @Transactional
    public void deleteAlbum(Long albumId) {
        Album album = getAndCheckAlbum(albumId);

        if(album.getProject() != null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Album is linked to a project and cannot be deleted");
        }


        albumRepository.delete(album);
    }

    @Override
    @Transactional
    public void addMediaToAlbum(AlbumMediaDTO albumMediaDTO) {
        Media media = getAndCheckMedia(albumMediaDTO.getMediaId());
        Album album = getAndCheckAlbum(albumMediaDTO.getAlbumId());

        if (album.getProject() != null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Album is linked to a project and cannot be edited");
        }

        boolean alreadyExists = album.getAlbumMedias().stream()
                .anyMatch(am -> am.getMedia().getId().equals(media.getId()));

        if (alreadyExists) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Media is already in the album");
        }

        album.addMedia(media);

        albumRepository.save(album);
    }

    @Override
    @Transactional
    public void addMediaToProjectAlbum(Long mediaId, MediaType mediaType, Long projectId) {
        Media media = getAndCheckMedia(mediaId);

        Album album = getAndCheckProject(projectId).getAlbum();

        if(album == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Album for the specified project doesn't exist");
        }

        boolean alreadyExists = album.getAlbumMedias().stream()
                .anyMatch(am -> am.getMedia().getId().equals(media.getId()));

        if (alreadyExists) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Media is already in the album");
        }

        album.addMedia(media);

        albumRepository.save(album);
    }

    @Override
    @Transactional
    public void editAlbum(AlbumEditDTO albumEditDTO) {
        Album album = getAndCheckAlbum(albumEditDTO.getAlbumId());

        if(album.getProject() != null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Album is linked to a project and cannot be edited");
        }

        if(albumEditDTO.getName() != null && !albumEditDTO.getName().isEmpty()) {
            album.setName(albumEditDTO.getName());
        }

        albumRepository.save(album);
    }

    @Override
    @Transactional
    public void removeMediaFromAlbum(AlbumMediaDTO albumMediaDTO) {
        Media media = getAndCheckMedia(albumMediaDTO.getMediaId());
        Album album = getAndCheckAlbum(albumMediaDTO.getAlbumId());

        if (album.getProject() != null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Album is linked to a project and cannot be edited");
        }

        AlbumMedia relation = album.getAlbumMedias().stream()
                .filter(am -> am.getMedia().getId().equals(media.getId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "Media is not in the album"));

        album.getAlbumMedias().remove(relation);

        albumMediaRepository.delete(relation);
    }

    @Override
    @Transactional
    public void unlinkProjectAlbum(Long projectId) {
        Project project = getAndCheckProject(projectId);

        if(project.getAlbum() != null) {
            Album album = project.getAlbum();
            album.setProject(null);
            project.setAlbum(null);

            albumRepository.save(album);
            projectRepository.save(project);
        }
    }

    @Override
    public AlbumDTO getAlbumById(Long albumId) {
        Album album = getAndCheckAlbum(albumId);

        List<Media> mediaList = album.getAlbumMedias().stream().map(AlbumMedia::getMedia).filter(Objects::nonNull).toList();
        refreshExpiredUrls(mediaList);

        return modelMapper.map(album, AlbumDTO.class);
    }

    @Override
    public List<AlbumDTO> getAllAlbums() {
        User currentUser = getCurrentUser();
        List<Album> albums = albumRepository.findByUser(currentUser);

        List<Media> allMedia = albums.stream()
                .flatMap(album -> album.getAlbumMedias().stream().map(AlbumMedia::getMedia))
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        refreshExpiredUrls(allMedia);

        return albums.stream().map(album -> modelMapper.map(album, AlbumDTO.class)).toList();
    }

    private void refreshExpiredUrls(List<Media> mediaList) {
        long now = System.currentTimeMillis();
        List<Media> toUpdate = new ArrayList<>();

        for (Media media : mediaList) {
            if (media.getPresignedMediaInfo() == null) {
                media.setPresignedMediaInfo(new PresignedMediaInfo());
            }
            if (media.getPresignedMediaInfo().getPresignedUrlExpireTime() < now) {
                String presignedUrl = persistenceService.getMediaUrl(media.getMediaPath());
                media.getPresignedMediaInfo().setMediaPresignedUrl(presignedUrl);
                media.getPresignedMediaInfo().setPresignedUrlExpireTime(now + (long) presignedAccessTimeInHours * 3600 * 1000);
                toUpdate.add(media);
            }
        }

        if (!toUpdate.isEmpty()) {
            toUpdate.sort(java.util.Comparator.comparing(Media::getId));
            mediaRepository.saveAll(toUpdate);
        }
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

    private User getCurrentUser() {
        return userRepository.findByEmail(AuthenticationUtils.getCurrentUserEmail())
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "User not found"));
    }
}
