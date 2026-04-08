package com.qorpy.api.dto.request.admin;

import com.qorpy.api.enums.AdminRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateAdminUserRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotNull(message = "Role is required")
    private AdminRole role;
}