package com.plansystem.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Login response DTO.
 */
@Data
@Builder
public class LoginResponse {

    private String token;
    private String tokenType;
    private long expiresIn;
    private UserInfo user;
}
