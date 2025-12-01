package com.Artiom.ArtifexAI.User.Service;

import com.Artiom.ArtifexAI.User.DTO.ChangePasswordRequest;
import com.Artiom.ArtifexAI.User.DTO.CreateNewPasswordRequest;
import com.Artiom.ArtifexAI.User.DTO.UserEditDTO;
import com.Artiom.ArtifexAI.User.DTO.UserRegisterDTO;
import com.Artiom.ArtifexAI.User.Model.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.text.ParseException;

public interface UserService extends UserDetailsService {
    void changePassword(ChangePasswordRequest changePasswordRequest);

    void sendResetPasswordEmail(String email);

    void createNewPassword(CreateNewPasswordRequest request);

    User register (UserRegisterDTO dto);

    User registerGoogle(String email);

    User registerGitHub(String email);

    void sendConfirmEmail();

    void confirmEmail(String token);

    User getUserByEmail(String email);

    User getCurrentUser();

    void editUser(UserEditDTO dto) throws ParseException;
}
