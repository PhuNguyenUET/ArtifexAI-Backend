package com.Artiom.ArtifexAI.Project.Repository;

import com.Artiom.ArtifexAI.Project.Model.Project;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ProjectRepository extends MongoRepository<Project, String> {
    List<Project> findAllByUserId(String userId);
}
