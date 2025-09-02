package com.LogManagementSystem.LogManager.Security;

import lombok.Data;

@Data
public class AuthenticationRequest {
    private String email, password;
}
