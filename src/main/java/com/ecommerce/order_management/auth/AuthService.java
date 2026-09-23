package com.ecommerce.order_management.auth;

import com.ecommerce.order_management.auth.dto.AuthResponse;
import com.ecommerce.order_management.auth.dto.LoginRequest;
import com.ecommerce.order_management.auth.dto.RefreshTokenRequest;
import com.ecommerce.order_management.auth.dto.RegisterRequest;
import com.ecommerce.order_management.entity.Role;
import com.ecommerce.order_management.entity.User;
import com.ecommerce.order_management.repository.UserRepository;
import com.ecommerce.order_management.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service d'authentification :
 * - register
 * - login
 * - refresh token
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;

    // inscription utilisateur
    public AuthResponse register(RegisterRequest request) {

        // normalisation email
        String email = request.email().trim().toLowerCase();

        // vérifier si email existe déjà
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email deja utilise");
        }

        // créer user
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .firstName(request.firstName())
                .lastName(request.lastName())
                .role(Role.ROLE_USER)
                .build();

        userRepository.save(user);

        return buildAuthResponse(user);
    }

    // login utilisateur
    public AuthResponse login(LoginRequest request) {

        String email = request.email().trim().toLowerCase();

        // authentification (Spring vérifie password)
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        email,
                        request.password()
                )
        );

        // récupérer user depuis DB
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return buildAuthResponse(user);
    }

    // refresh token
    public AuthResponse refreshToken(RefreshTokenRequest request) {

        String token = request.refreshToken();

        // extraire username depuis token
        String username = jwtUtil.extractUsername(token);

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // vérifier validité du token
        if (!jwtUtil.isTokenValid(token, user)) {
            throw new RuntimeException("Invalid refresh token");
        }

        return buildAuthResponse(user);
    }

    // construire réponse avec tokens (record → new)
    private AuthResponse buildAuthResponse(User user) {
        return new AuthResponse(
                jwtUtil.generateAccessToken(user),
                jwtUtil.generateRefreshToken(user),
                user.getEmail(),
                user.getRole().name()
        );
    }
}