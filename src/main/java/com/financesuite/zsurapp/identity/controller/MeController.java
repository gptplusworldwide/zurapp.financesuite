
package com.financesuite.zsurapp.identity.controller;

import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me")
public class MeController {

    @GetMapping
    public MeResponse getCurrentUser(
            @AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(
            jwt.getSubject()
        );

        return new MeResponse(userId);
    }

    public record MeResponse(UUID id) {
    }
}
