package com.Artiom.ArtifexAI.User.DTO;

import com.Artiom.ArtifexAI.User.Model.AuthProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDTO {
    private Long id;

    private String email;
    private AuthProvider authProvider;

    private String firstName;
    private String lastName;

    private Date dateOfBirth;
    private boolean isEmailValidated;
}
