package com.Artiom.ArtifexAI.User.Service.Impl;

import com.Artiom.ArtifexAI.Common.Exception.BusinessException;
import com.Artiom.ArtifexAI.Mail.Model.MailType;
import com.Artiom.ArtifexAI.Mail.Service.MailSend.SendMailService;
import com.Artiom.ArtifexAI.Mail.Service.MailTemplate.MailTemplateService;
import com.Artiom.ArtifexAI.User.DTO.*;
import com.Artiom.ArtifexAI.User.Model.AuthProvider;
import com.Artiom.ArtifexAI.User.Model.CustomUserDetails;
import com.Artiom.ArtifexAI.User.Model.Role;
import com.Artiom.ArtifexAI.User.Model.User;
import com.Artiom.ArtifexAI.User.Repository.UserRepository;
import com.Artiom.ArtifexAI.User.Service.UserService;
import com.Artiom.ArtifexAI.Util.AuthenticationUtils;
import com.Artiom.ArtifexAI.Util.RandomUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Optional;
import java.util.UUID;

@Service
@Primary
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final MailTemplateService mailTemplateService;
    private final SendMailService sendMailService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private ModelMapper modelMapper;

    @PostConstruct
    private void setupModelMapper() {
        modelMapper = new ModelMapper();

        modelMapper.createTypeMap(User.class, UserResponseDTO.class)
                .setConverter(context -> {
                    User user = context.getSource();

                    return UserResponseDTO.builder()
                            .id(user.getId())
                            .email(user.getEmail())
                            .authProvider(user.getAuthProvider())
                            .firstName(user.getFirstName())
                            .lastName(user.getLastName())
                            .dateOfBirth(user.getDateOfBirth())
                            .isEmailValidated(user.isEmailValidated())
                            .build();
                });
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new CustomUserDetails(user, true, true, true, true);
    }

    @Override
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public UserResponseDTO register(UserRegisterDTO dto) {
        String password = dto.getPassword();
        String email = dto.getEmail();
        userRepository.findByEmail(dto.getEmail()).ifPresent(user -> {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "User already existed");
        });
        User user = new User();
        user.setAuthProvider(AuthProvider.LOCAL);
        user.setRole(Role.USER);
        user.setEmail(email);
        user.setEmailValidated(false);
        user.setPassword(bCryptPasswordEncoder.encode(password));
        user.setActive(true);
        return modelMapper.map(userRepository.save(user), UserResponseDTO.class);
    }

    @Override
    public User registerGoogle(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "User already existed");
        });
        User user = new User();
        user.setAuthProvider(AuthProvider.GOOGLE);
        user.setRole(Role.USER);
        user.setEmail(email);
        user.setEmailValidated(true);
        user.setActive(true);
        user.setEmailValidated(true);
        return userRepository.save(user);
    }

    @Override
    public User registerGitHub(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "User already existed");
        });
        User user = new User();
        user.setAuthProvider(AuthProvider.GITHUB);
        user.setRole(Role.USER);
        user.setEmail(email);
        user.setEmailValidated(true);
        user.setActive(true);
        user.setEmailValidated(true);
        return userRepository.save(user);
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    static final String RESET_PASSWORD_SUBJECT = "ArtifexAI - Reset Password";

    @Override
    public void sendResetPasswordEmail(String email) {
        Optional<User> u = userRepository.findByEmail(email);
        if (u.isEmpty()) {
            throw BusinessException.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("User does not exist!")
                    .build();
        }
        User user = u.get();

        if(!user.getAuthProvider().equals(AuthProvider.LOCAL)) {
            throw BusinessException.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("Password reset is not available for OAuth2 users!")
                    .build();
        }

        if(!user.isEmailValidated()) {
            throw BusinessException.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("Email is not validated!")
                    .build();
        }

        String token = user.getId() + "-" + UUID.randomUUID() + "-" + System.currentTimeMillis();
        user.setResetPasswordToken(token);
        user.setResetPasswordTokenExpire(System.currentTimeMillis() + 15 * 60 * 1000);
        userRepository.save(user);
        sendResetPasswordMail(email, token);
    }

    private void sendResetPasswordMail(String email, String token) {
        String template = mailTemplateService.getTemplate(MailType.RESET_PASSWORD);
        String content = template.replace("{NEW_PASSWORD_TOKEN}", token);
        sendMailService.addToQueue(email, RESET_PASSWORD_SUBJECT, content);
    }

    private void sendEmailConfirmation(String email, String token) {
        String template = mailTemplateService.getTemplate(MailType.CONFIRM_EMAIL);
        String content = template.replace("{EMAIL_CODE}", token);
        sendMailService.addToQueue(email, CONFIRM_EMAIL_SUBJECT, content);
    }

    @Override
    public void changePassword(ChangePasswordRequest changePasswordRequest) {
        User user = getCurrentUser();
        if (!bCryptPasswordEncoder.matches(changePasswordRequest.getOldPassword(), user.getPassword())) {
            throw BusinessException.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("Wrong current password")
                    .build();
        }

        if(!user.getAuthProvider().equals(AuthProvider.LOCAL)) {
            throw BusinessException.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("Password change is not available for OAuth2 users!")
                    .build();
        }

        user.setPassword(bCryptPasswordEncoder.encode(changePasswordRequest.getNewPassword()));
        userRepository.save(user);

    }

    static final String CONFIRM_EMAIL_SUBJECT = "ArtifexAI - Confirm Email";

    @Override
    public void sendConfirmEmail() {
        User user = getCurrentUser();
        String email = user.getEmail();
        String token = RandomUtils.generateRandomString(7);
        user.setConfirmEmailToken(token);
        user.setConfirmEmailTokenExpire(System.currentTimeMillis() + 30 * 60 * 1000);
        userRepository.save(user);
        sendEmailConfirmation(email, token);
    }

    @Override
    public void confirmEmail(String token) {
        User user = getCurrentUser();
        if (!StringUtils.equals(user.getConfirmEmailToken(), token)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Token does not exist!");
        }
        if (System.currentTimeMillis() > user.getConfirmEmailTokenExpire()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Token has expired.");
        }

        user.setEmailValidated(true);
        userRepository.save(user);
    }

    @Override
    public User getCurrentUser() {
        String email = AuthenticationUtils.getCurrentUserEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
    }

    @Override
    public UserResponseDTO getCurrentUserDTO() {
        return modelMapper.map(getCurrentUser(), UserResponseDTO.class);
    }

    @Override
    public void editUser(UserEditDTO dto) throws ParseException {
        User user = getCurrentUser();

        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setDateOfBirth(dto.getDateOfBirth());

        userRepository.save(user);
    }

    @Override
    public void createNewPassword(CreateNewPasswordRequest request) {
        String token = request.getToken();
        String password = request.getPassword();
        Long userId = extractUserId(token);

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Token doesn't exist!");
        }
        if (!StringUtils.equals(user.getResetPasswordToken(), token)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Token doesn't exist!");
        }
        if (System.currentTimeMillis() > user.getResetPasswordTokenExpire()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Token has expired.");
        }

        if(!user.getAuthProvider().equals(AuthProvider.LOCAL)) {
            throw BusinessException.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("Password reset is not available for OAuth2 users!")
                    .build();
        }

        user.setPassword(bCryptPasswordEncoder.encode(password));
        user.setActive(true);
        user.setResetPasswordToken(null);
        user.setResetPasswordTokenExpire(-1);
        userRepository.save(user);
    }

    private static Long extractUserId(String token) {
        try {
            return Long.valueOf(StringUtils.substring(token, 0, StringUtils.indexOf(token, "-")));
        } catch (NumberFormatException e) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Token doesn't exist!");
        }
    }
}
