package com.marriagehall.auth_service.dto;

import com.marriagehall.auth_service.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserProfileResponse {
    private UUID id;
    private String name;
    private String email;
    private Role role;
    private String phone;
    private String avatarUrl;
    private LocalDateTime createdAt;
}
