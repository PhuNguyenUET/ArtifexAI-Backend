package com.Artiom.ArtifexAI.Authentication.Controller;

import com.Artiom.ArtifexAI.Authentication.DTO.AuthenticationRequest;
import com.Artiom.ArtifexAI.Authentication.DTO.AuthenticationResponse;
import com.Artiom.ArtifexAI.Authentication.Service.AuthenticationService;
import com.Artiom.ArtifexAI.Common.Api.ApiResponse;
import com.Artiom.ArtifexAI.User.DTO.UserRegisterDTO;
import com.Artiom.ArtifexAI.User.Service.UserService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "user_authentication")
@RequestMapping("/api/user/v1")
public class AuthenticationController {
    @Value("${api.token}")
    private String apiToken;

    private final AuthenticationService userAuthenticationService;
    private final UserService userService;

    @PostMapping("/authenticate")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = AuthenticationResponse.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> authentication (@RequestHeader("X-auth-token") String token,
                                                       @RequestBody AuthenticationRequest request) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            return ResponseEntity.ok(ApiResponse.success("Authentication successful", userAuthenticationService.authenticate(request)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register (@RequestHeader("X-auth-token") String token,
                                                 @RequestBody UserRegisterDTO request) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            userService.register(request);
            return ResponseEntity.ok(ApiResponse.success("Registration success"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PostMapping("/refresh_jwt")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = String.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> refreshToken (@RequestHeader("X-auth-token") String token,
                                                     @RequestBody String refresh) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            String jwtToken = userAuthenticationService.refreshToken(refresh);
            if (jwtToken == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse(HttpServletResponse.SC_UNAUTHORIZED, "Invalid refresh token"));
            }
            return ResponseEntity.ok(ApiResponse.success(jwtToken));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @GetMapping("/authenticate_oauth2")
    public ResponseEntity<ApiResponse> authenticateOAuth2 (@AuthenticationPrincipal OAuth2User principal,
                                                           Authentication authentication) {
        if(!(authentication instanceof OAuth2AuthenticationToken authToken)) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, "Wrong authentication type"));
        }

        String provider = authToken.getAuthorizedClientRegistrationId().toUpperCase();
        return ResponseEntity.ok(ApiResponse.success("Authentication successful",
                userAuthenticationService.authenticateOAuth2(principal, provider)));
    }
}
