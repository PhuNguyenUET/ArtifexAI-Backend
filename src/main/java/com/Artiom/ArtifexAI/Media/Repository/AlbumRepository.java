package com.Artiom.ArtifexAI.Media.Repository;

import com.Artiom.ArtifexAI.Media.Model.Album;
import com.Artiom.ArtifexAI.User.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlbumRepository extends JpaRepository<Album, Long> {
    List<Album> findByUser(User user);
}
