package com.qorpy.api.dto.request.alert;

import com.qorpy.api.enums.AlertSeverity;
import com.qorpy.api.enums.AlertTriggerType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class UpdateAlertRuleRequest {
    @NotBlank(message = "Rule name is required")
    private String name;

    @NotNull(message = "Trigger type is required")
    private AlertTriggerType triggerType;

    @NotNull(message = "Threshold value is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Threshold must be greater than 0")
    private BigDecimal thresholdValue;

    @NotNull(message = "Severity is required")
    private AlertSeverity severity;
}