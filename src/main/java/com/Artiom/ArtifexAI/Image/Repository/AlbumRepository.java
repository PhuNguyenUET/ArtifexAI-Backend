package com.Artiom.ArtifexAI.Image.Repository;

import com.Artiom.ArtifexAI.Image.Model.Album;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface AlbumRepository extends MongoRepository<Album, String> {
    List<Album> findAllByUserId(String userId);

    Optional<Album> findByProjectId(String projectId);
}
