package com.marriagehall.auth_service.controller;

import com.marriagehall.auth_service.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.marriagehall.auth_service.dto.*;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request ) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @org.springframework.web.bind.annotation.GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getCurrentUser(
            @org.springframework.web.bind.annotation.RequestHeader(value = "X-User-Id", required = false) String userId,
            @org.springframework.web.bind.annotation.RequestHeader(value = "X-Email", required = false) String email,
            @org.springframework.web.bind.annotation.RequestHeader(value = org.springframework.http.HttpHeaders.AUTHORIZATION, required = false) String authHeader
    ) {
        return ResponseEntity.ok(authService.getCurrentUserProfile(userId, email, authHeader));
    }
}
