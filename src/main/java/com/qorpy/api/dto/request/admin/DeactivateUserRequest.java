package com.qorpy.api.dto.request.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeactivateUserRequest {

    @NotBlank(message = "Reason for deactivation is required")
    private String reason;
}