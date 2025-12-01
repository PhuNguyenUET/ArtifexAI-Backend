package com.Artiom.ArtifexAI.Authentication.Service.Impl;

import com.Artiom.ArtifexAI.Authentication.DTO.AuthenticationRequest;
import com.Artiom.ArtifexAI.Authentication.DTO.AuthenticationResponse;
import com.Artiom.ArtifexAI.Authentication.Filter.JwtConstant;
import com.Artiom.ArtifexAI.Authentication.Service.AuthenticationService;
import com.Artiom.ArtifexAI.Common.Exception.BusinessException;
import com.Artiom.ArtifexAI.Common.Exception.LockedUserException;
import com.Artiom.ArtifexAI.Common.Exception.UserNotFoundException;
import com.Artiom.ArtifexAI.Common.Exception.WrongPasswordException;
import com.Artiom.ArtifexAI.User.Model.AuthProvider;
import com.Artiom.ArtifexAI.User.Model.CustomUserDetails;
import com.Artiom.ArtifexAI.User.Model.User;
import com.Artiom.ArtifexAI.User.Service.UserService;
import com.Artiom.ArtifexAI.Util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserService userService;
    private final MongoTemplate mongoTemplate;
    private final AuthenticationManager authenticationManager;

    private final LockedUserException lockedUserException = new LockedUserException("Too many wrong attempts. Account has already been locked.");
    private final WrongPasswordException wrongPasswordException = new WrongPasswordException("Wrong password");

    @Override
    public AuthenticationResponse authenticate (AuthenticationRequest request) {
        final int FAILURE_LIMIT = 5;
        User user = userService.getUserByEmail(request.getEmail());

        if(user == null) {
            throw new UserNotFoundException(request.getEmail());
        }

        if(!Objects.equals(user.getAuthProvider(), AuthProvider.LOCAL.toString())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Wrong authentication provider");
        }

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (BadCredentialsException e) {
            user.setFailedAttempt(user.getFailedAttempt() + 1);
            if (user.getFailedAttempt() > FAILURE_LIMIT) {
                user.setFailedAttempt(0);;
                user.setActive(false);
            }
            mongoTemplate.save(user);
            if(!user.isActive()) {
                throw lockedUserException;
            }
            throw wrongPasswordException;
        }
        if(!user.isActive()) {
            throw lockedUserException;
        }

        user.setFailedAttempt(0);
        mongoTemplate.save(user);

        String jwt = JwtUtils.generateJwtToken(user.getEmail());
        String refresh = JwtUtils.generateRefreshToken(user.getEmail());
        return new AuthenticationResponse(jwt, refresh);
    }

    @Override
    public String refreshToken(String refreshToken) {
        if (refreshToken != null && refreshToken.startsWith(JwtConstant.JWT_TOKEN_PREFIX)) {
            String token = refreshToken.substring(JwtConstant.JWT_TOKEN_PREFIX.length());
            String username;

            try {
                username = JwtUtils.extractRefreshUsername(token);
            } catch (Exception e) {
                return null;
            }

            CustomUserDetails customUserDetails;

            try {
                customUserDetails = (CustomUserDetails) userService.loadUserByUsername(username);
            } catch (UsernameNotFoundException e) {
                return null;
            }

            if(!customUserDetails.getUser().isActive()) {
                return null;
            }

            if (JwtUtils.validateRefreshToken(token, customUserDetails)) {
                return JwtUtils.generateJwtToken(username);
            }
        }

        return null;
    }

    @Override
    public AuthenticationResponse authenticateOAuth2(OAuth2User principal, String authenticationProvider) {
        Map<String, Object> attributes = principal.getAttributes();

        String email;

        if(authenticationProvider.equals(AuthProvider.GOOGLE.toString())) {
            email = attributes.get("email").toString();
        } else if(authenticationProvider.equals(AuthProvider.GITHUB.toString())) {
            email = attributes.get("id").toString();
        } else {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Unsupported authentication provider");
        }

        User user = userService.getUserByEmail(email);

        if(user == null) {
            if (authenticationProvider.equals(AuthProvider.GOOGLE.toString())) {
                user = userService.registerGoogle(email);
            } else {
                user = userService.registerGitHub(email);
            }
        }

        String jwt = JwtUtils.generateJwtToken(user.getEmail());
        String refresh = JwtUtils.generateRefreshToken(user.getEmail());
        return new AuthenticationResponse(jwt, refresh);
    }
}
