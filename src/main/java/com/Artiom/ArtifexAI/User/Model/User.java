package com.Artiom.ArtifexAI.User.Model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Setter
@Getter
@Document("z_user")
public class User {
    @Id
    private String id;

    @Indexed(background = true)
    private String email;
    private String password;

    private String authProvider;

    private String role;
    private boolean active;
    private String firstName;
    private String lastName;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private Date dateOfBirth = new Date();

    private int failedAttempt;

    private String resetPasswordToken;
    private long resetPasswordTokenExpire;

    private boolean isEmailValidated;
    private String confirmEmailToken;
    private long confirmEmailTokenExpire;
}