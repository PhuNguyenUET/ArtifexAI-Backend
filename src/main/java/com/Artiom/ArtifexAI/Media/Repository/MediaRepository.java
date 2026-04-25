package com.Artiom.ArtifexAI.Media.Repository;

import com.Artiom.ArtifexAI.Media.Model.Media;
import com.Artiom.ArtifexAI.User.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MediaRepository extends JpaRepository<Media, Long> {
    List<Media> findByUser(User user);
}
