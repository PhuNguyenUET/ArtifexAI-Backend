package com.Artiom.ArtifexAI.Media.Repository;

import com.Artiom.ArtifexAI.Media.Model.Media;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MediaRepository extends MongoRepository<Media, String> {
    List<Media> findAllByUserId(String userId);
}
