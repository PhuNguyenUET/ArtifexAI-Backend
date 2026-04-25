package com.Artiom.ArtifexAI.Project.Repository;

import com.Artiom.ArtifexAI.Project.Model.Project;
import com.Artiom.ArtifexAI.User.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByUser(User user);
}
