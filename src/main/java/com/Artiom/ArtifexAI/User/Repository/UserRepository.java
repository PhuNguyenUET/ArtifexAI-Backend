package com.Artiom.ArtifexAI.User.Repository;

import com.Artiom.ArtifexAI.User.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
