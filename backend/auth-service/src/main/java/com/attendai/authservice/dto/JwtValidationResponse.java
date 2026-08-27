package com.attendai.authservice.dto;

import java.util.List;

public record JwtValidationResponse(
        boolean valid,
        String subject,
        List<String> roles) {
}
