package com.Artiom.ArtifexAI.Image.Service.Impl;

import com.Artiom.ArtifexAI.Common.Exception.BusinessException;
import com.Artiom.ArtifexAI.Image.DTO.ImageDTO;
import com.Artiom.ArtifexAI.Image.Model.Album;
import com.Artiom.ArtifexAI.Image.Model.Image;
import com.Artiom.ArtifexAI.Image.Repository.AlbumRepository;
import com.Artiom.ArtifexAI.Image.Repository.ImageRepository;
import com.Artiom.ArtifexAI.Image.Service.ImageService;
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
public class ImageServiceImpl implements ImageService {
    @Value("${aws.cloudfront.url-access-time}")
    private int presignedAccessTimeInHours;

    private final ImageRepository imageRepository;
    private final AlbumRepository albumRepository;
    private final PersistenceService persistenceService;

    private ModelMapper modelMapper;

    @PostConstruct
    private void setupModelMapper() {
        modelMapper = new ModelMapper();

        modelMapper.createTypeMap(Image.class, ImageDTO.class)
                .setConverter(context -> {
                    Image source = context.getSource();

                    if(source.getPresignedImageInfo() == null) {
                        source.setPresignedImageInfo(new com.Artiom.ArtifexAI.Image.Model.PresignedImageInfo());
                    }

                    if(source.getPresignedImageInfo().getPresignedUrlExpireTime() < System.currentTimeMillis()) {
                        String presignedUrl = persistenceService.getImageUrl(source.getImagePath());
                        source.getPresignedImageInfo().setImagePresignedUrl(presignedUrl);
                        source.getPresignedImageInfo().setPresignedUrlExpireTime(System.currentTimeMillis() + (long) presignedAccessTimeInHours * 3600 * 1000);

                        imageRepository.save(source);
                    }

                    return ImageDTO.builder()
                            .id(source.getId())
                            .imageUrl(source.getPresignedImageInfo().getImagePresignedUrl())
                            .createdDate(source.getCreatedDate())
                            .build();
                });
    }

    @Override
    public ImageDTO addImage(String imagePath) {
        User currentUser = AuthenticationUtils.getCurrentUser();

        Image image = Image.builder()
                .imagePath(imagePath)
                .userId(currentUser.getId())
                .build();

        Image savedImage = imageRepository.save(image);
        return modelMapper.map(savedImage, ImageDTO.class);
    }

    @Override
    public void deleteImage(String imageId) {
        Image image = getAndCheckImage(imageId);

        persistenceService.deleteImageFromPersistence(image.getImagePath());
        imageRepository.delete(image);
    }

    @Override
    public ImageDTO getImageById(String imageId) {
        Image image = getAndCheckImage(imageId);

        return modelMapper.map(image, ImageDTO.class);
    }

    @Override
    public List<ImageDTO> getGallery() {
        User currentUser = AuthenticationUtils.getCurrentUser();

        return imageRepository.findAllByUserId(currentUser.getId()).stream().map(image -> modelMapper.map(image, ImageDTO.class)).collect(Collectors.toList());
    }

    @Override
    public List<ImageDTO> getImagesByAlbum(String albumId) {
        User currentUser = AuthenticationUtils.getCurrentUser();

        Album album = albumRepository.findById(albumId).orElse(null);
        if(album == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Album doesn't exist");
        }

        if(!album.getUserId().equals(currentUser.getId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "You are not the owner of this album");
        }

        List<Image> images = imageRepository.findAllById(album.getImages());

        boolean changedContent = false;

        List<String> imageIds = album.getImages();
        for (String imageId : imageIds) {
            Image image = imageRepository.findById(imageId).orElse(null);
            if (image != null && image.getUserId().equals(currentUser.getId())) {
                images.add(image);
            } else {
                album.getImages().remove(imageId);
                changedContent = true;
            }
        }

        if(changedContent) {
            albumRepository.save(album);
        }

        return images.stream().map(image -> modelMapper.map(image, ImageDTO.class)).collect(Collectors.toList());
    }

    private Image getAndCheckImage(String imageId) {
        User currentUser = AuthenticationUtils.getCurrentUser();

        Image image = imageRepository.findById(imageId).orElse(null);
        if(image == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Image doesn't exist");
        }

        if(!image.getUserId().equals(currentUser.getId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "You are not the owner of this image");
        }
        return image;
    }
}
