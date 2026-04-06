package com.qorpy.api.dto.response;

import com.qorpy.api.enums.AdminRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String type = "Bearer";

    private String fullName;
    private String email;
    private AdminRole role;
}