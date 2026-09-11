package com.marriagehall.auth_service.service;

import com.marriagehall.auth_service.dto.AuthResponse;
import com.marriagehall.auth_service.dto.LoginRequest;
import com.marriagehall.auth_service.dto.SignupRequest;
import com.marriagehall.auth_service.exception.EmailAlreadyExistsException;
import com.marriagehall.auth_service.exception.InvalidCredentialsException;
import com.marriagehall.auth_service.exception.UserNotFoundException;
import com.marriagehall.auth_service.model.User;
import com.marriagehall.auth_service.repository.UserRepository;
import com.marriagehall.auth_service.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Transactional
    public AuthResponse register(SignupRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException("Email already exists");
        }
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setPhone(request.getPhone());
        try {
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new EmailAlreadyExistsException("Email already exists");
        }

        String token = jwtUtils.generateToken(user);
        return AuthResponse.builder()
                .message("User registered successfully")
                .token(token)
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // 2. Match Password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }
        // 3. Generate Token
        String token = jwtUtils.generateToken(user);
        return AuthResponse.builder()
                .message("Login successful")
                .token(token)
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }

    public com.marriagehall.auth_service.dto.UserProfileResponse getCurrentUserProfile(String headerUserId, String headerEmail, String authHeader) {
        User user = null;
        if (headerUserId != null && !headerUserId.isBlank()) {
            try {
                user = userRepository.findById(java.util.UUID.fromString(headerUserId)).orElse(null);
            } catch (Exception ignored) {}
        }
        if (user == null && headerEmail != null && !headerEmail.isBlank()) {
            user = userRepository.findByEmail(headerEmail).orElse(null);
        }
        if (user == null && authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtils.validateToken(token)) {
                String email = jwtUtils.extractEmail(token);
                user = userRepository.findByEmail(email).orElse(null);
            }
        }
        if (user == null) {
            // Check Spring Security Context
            org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getName() != null) {
                user = userRepository.findByEmail(auth.getName()).orElse(null);
            }
        }
        if (user == null) {
            throw new UserNotFoundException("User not found");
        }

        return com.marriagehall.auth_service.dto.UserProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
