package com.qorpy.api.dto.response;

import com.qorpy.api.enums.AdminRole;
import com.qorpy.api.enums.AdminStatus;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class AdminUserDto {
    private UUID id;
    private String fullName;
    private String email;
    private AdminRole role;
    private AdminStatus status;
    private OffsetDateTime lastLoginAt;
    private OffsetDateTime createdAt;
}