package com.qorpy.api.dto.request.taxpayer;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SuspendTaxpayerRequest {
    @NotBlank(message = "Suspension reason is required")
    private String reason;
}