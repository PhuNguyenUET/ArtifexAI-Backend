package com.Artiom.ArtifexAI.Health;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class JwtController {
    @GetMapping("/jwt_check")
    public ResponseEntity<String> jwtCheck() {
        return ResponseEntity.ok("OK");
    }
}
