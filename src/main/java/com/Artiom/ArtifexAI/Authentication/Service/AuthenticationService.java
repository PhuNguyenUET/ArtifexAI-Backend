package com.Artiom.ArtifexAI.Authentication.Service;

import com.Artiom.ArtifexAI.Authentication.DTO.AuthenticationRequest;
import com.Artiom.ArtifexAI.Authentication.DTO.AuthenticationResponse;
import org.springframework.security.oauth2.core.user.OAuth2User;

public interface AuthenticationService {
    AuthenticationResponse authenticate(AuthenticationRequest request);

    AuthenticationResponse authenticateOAuth2(OAuth2User principal, String authenticationProvider);

    String refreshToken(String refreshToken);
}
