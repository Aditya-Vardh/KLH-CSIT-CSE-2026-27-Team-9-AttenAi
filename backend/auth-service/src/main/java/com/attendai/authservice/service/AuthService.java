package com.attendai.authservice.service;

import com.attendai.authservice.dto.AuthResponse;
import com.attendai.authservice.dto.ChangePasswordRequest;
import com.attendai.authservice.dto.ForgotPasswordRequest;
import com.attendai.authservice.dto.LoginRequest;
import com.attendai.authservice.dto.RefreshTokenRequest;
import com.attendai.authservice.dto.RegisterRequest;
import com.attendai.authservice.dto.ResetPasswordRequest;
import com.attendai.authservice.entity.PasswordResetToken;
import com.attendai.authservice.entity.RefreshToken;
import com.attendai.authservice.entity.User;
import com.attendai.authservice.exception.BadRequestException;
import com.attendai.authservice.exception.ResourceNotFoundException;
import com.attendai.authservice.repository.PasswordResetTokenRepository;
import com.attendai.authservice.repository.RefreshTokenRepository;
import com.attendai.authservice.repository.UserRepository;
import com.attendai.authservice.security.JwtService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final long refreshTokenDays;

    public AuthService(UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordResetTokenRepository passwordResetTokenRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       @Value("${attendai.security.refresh-token-days}") long refreshTokenDays) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenDays = refreshTokenDays;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email().toLowerCase())) {
            throw new BadRequestException("Email already registered");
        }
        User user = new User();
        user.setEmail(request.email().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setRole(request.role());
        user = userRepository.save(user);
        return issueTokens(user);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        User user = userRepository.findByEmail(request.email().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return issueTokens(user);
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.refreshToken())
                .filter(token -> !token.isRevoked())
                .filter(token -> token.getExpiresAt().isAfter(Instant.now()))
                .orElseThrow(() -> new BadRequestException("Refresh token is invalid or expired"));
        return issueTokens(refreshToken.getUser());
    }

    public String createForgotPasswordToken(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.email().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setToken(UUID.randomUUID().toString());
        token.setExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS));
        passwordResetTokenRepository.save(token);
        return token.getToken();
    }

    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken token = passwordResetTokenRepository.findByToken(request.resetToken())
                .filter(resetToken -> !resetToken.isUsed())
                .filter(resetToken -> resetToken.getExpiresAt().isAfter(Instant.now()))
                .orElseThrow(() -> new BadRequestException("Reset token is invalid or expired"));
        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        token.setUsed(true);
        passwordResetTokenRepository.save(token);
    }

    public boolean validateToken(String token) {
        return jwtService.isTokenValid(token);
    }

    /**
     * Self-service password change — requires the caller's current password for verification.
     * The caller's email is extracted from the validated JWT (forwarded by the gateway as X-Auth-User-Email).
     */
    public void changePassword(String callerEmail, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(callerEmail.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }
        if (request.currentPassword().equals(request.newPassword())) {
            throw new BadRequestException("New password must differ from the current password");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    private AuthResponse issueTokens(User user) {
        String accessToken = jwtService.generateAccessToken(user.getEmail(), user.getRole(), user.getId());
        String refreshTokenValue = UUID.randomUUID().toString();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(refreshTokenValue);
        refreshToken.setExpiresAt(Instant.now().plus(refreshTokenDays, ChronoUnit.DAYS));
        refreshTokenRepository.save(refreshToken);

        return new AuthResponse(
                accessToken,
                refreshTokenValue,
                "Bearer",
                Instant.now().plus(60, ChronoUnit.MINUTES),
                new AuthResponse.UserSummary(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getRole().name()));
    }
}
