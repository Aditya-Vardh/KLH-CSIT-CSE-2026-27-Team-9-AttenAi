package com.attendai.authservice.dto;

import java.time.Instant;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        Instant expiresAt,
        UserSummary user) {

    public static record UserSummary(Long id, String email, String firstName, String lastName, String role) {
    }
}
