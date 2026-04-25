package com.Artiom.ArtifexAI.User.Model;

import com.Artiom.ArtifexAI.Media.Model.Album;
import com.Artiom.ArtifexAI.Media.Model.Media;
import com.Artiom.ArtifexAI.Project.Model.Project;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_user_email", columnList = "email")
        }
)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", columnDefinition = "TEXT", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private AuthProvider authProvider;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Media> medias = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Album> albums = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Project> projects = new ArrayList<>();

    private boolean active;
    private String firstName;
    private String lastName;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Builder.Default
    private Date dateOfBirth = new Date();

    private int failedAttempt;

    @Column(columnDefinition = "TEXT")
    private String resetPasswordToken;
    private long resetPasswordTokenExpire;

    private boolean isEmailValidated;
    @Column(columnDefinition = "TEXT")
    private String confirmEmailToken;
    private long confirmEmailTokenExpire;
}