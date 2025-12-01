package com.Artiom.ArtifexAI.Image.Repository;

import com.Artiom.ArtifexAI.Image.Model.Image;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ImageRepository extends MongoRepository<Image, String> {
    List<Image> findAllByUserId(String userId);
}
