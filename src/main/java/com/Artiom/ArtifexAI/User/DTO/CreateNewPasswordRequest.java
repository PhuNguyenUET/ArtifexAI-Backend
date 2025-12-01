package com.Artiom.ArtifexAI.User.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateNewPasswordRequest {
    String token;
    String password;
}
