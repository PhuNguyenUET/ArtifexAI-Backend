package com.Artiom.ArtifexAI.User.Service;

import com.Artiom.ArtifexAI.User.DTO.*;
import com.Artiom.ArtifexAI.User.Model.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.text.ParseException;

public interface UserService extends UserDetailsService {
    User saveUser(User user);

    void changePassword(ChangePasswordRequest changePasswordRequest);

    void sendResetPasswordEmail(String email);

    void createNewPassword(CreateNewPasswordRequest request);

    UserResponseDTO register (UserRegisterDTO dto);

    User registerGoogle(String email);

    User registerGitHub(String email);

    void sendConfirmEmail();

    void confirmEmail(String token);

    User getUserByEmail(String email);

    User getCurrentUser();

    UserResponseDTO getCurrentUserDTO();

    void editUser(UserEditDTO dto) throws ParseException;
}
