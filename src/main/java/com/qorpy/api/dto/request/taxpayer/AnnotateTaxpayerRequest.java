package com.qorpy.api.dto.request.taxpayer;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AnnotateTaxpayerRequest {
    @NotBlank(message = "Note is required")
    private String note;

    private boolean isFlag = false;
}