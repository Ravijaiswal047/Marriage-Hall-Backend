package com.marriagehall.auth_service.dto;

import com.marriagehall.auth_service.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponse {

    private String message;
    private String token;
    private UUID userId;
    private String name;
    private String email;
    private Role role;
    private String phone;
    private String avatarUrl;

    public AuthResponse(String message, String token) {
        this.message = message;
        this.token = token;
    }
}
