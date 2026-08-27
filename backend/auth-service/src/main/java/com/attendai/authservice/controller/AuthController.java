package com.attendai.authservice.controller;

import com.attendai.authservice.dto.AuthResponse;
import com.attendai.authservice.dto.ChangePasswordRequest;
import com.attendai.authservice.dto.ForgotPasswordRequest;
import com.attendai.authservice.dto.JwtValidationResponse;
import com.attendai.authservice.dto.LoginRequest;
import com.attendai.authservice.dto.RefreshTokenRequest;
import com.attendai.authservice.dto.RegisterRequest;
import com.attendai.authservice.dto.ResetPasswordRequest;
import com.attendai.authservice.service.AuthService;
import com.attendai.authservice.security.JwtService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return authService.refreshToken(request);
    }

    @PostMapping("/forgot-password")
    public Map<String, String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return Map.of("resetToken", authService.createForgotPasswordToken(request));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/validate")
    public JwtValidationResponse validate(@RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.startsWith("Bearer ") ? authorizationHeader.substring(7) : authorizationHeader;
        boolean valid = authService.validateToken(token);
        if (!valid) return new JwtValidationResponse(false, null, List.of());
        String subject = jwtService.parseClaims(token).getSubject();
        List<String> roles = jwtService.extractRoles(token);
        return new JwtValidationResponse(true, subject, roles);
    }

    /**
     * Self-service password change — requires the current password for verification.
     * The caller's identity is taken from the X-Auth-User-Email header injected by the API Gateway.
     */
    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @RequestHeader("X-Auth-User-Email") String callerEmail) {
        authService.changePassword(callerEmail, request);
        return ResponseEntity.noContent().build();
    }
}
