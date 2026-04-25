package com.Artiom.ArtifexAI.Authentication.Config;

import com.Artiom.ArtifexAI.Authentication.DTO.AuthenticationResponse;
import com.Artiom.ArtifexAI.Authentication.Service.AuthenticationService;
import com.Artiom.ArtifexAI.User.Model.AuthProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final AuthenticationService authenticationService;

    @Value("${oauth2.mobile.redirect-uri}")
    private String mobileRedirectUri;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        if (!(authentication instanceof OAuth2AuthenticationToken authToken)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Wrong authentication type");
            return;
        }

        String providerStr = authToken.getAuthorizedClientRegistrationId().toUpperCase();
        AuthProvider provider;
        try {
            provider = AuthProvider.valueOf(providerStr);
        } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unsupported authentication provider: " + providerStr);
            return;
        }
        OAuth2User principal = (OAuth2User) authentication.getPrincipal();

        AuthenticationResponse authResponse = authenticationService.authenticateOAuth2(principal, provider);

        String redirectUrl = UriComponentsBuilder.fromUriString(mobileRedirectUri)
                .queryParam("jwt", authResponse.getJwtToken())
                .queryParam("refresh", authResponse.getRefreshToken())
                .build().toUriString();

        response.sendRedirect(redirectUrl);
    }
}
