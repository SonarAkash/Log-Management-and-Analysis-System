package com.LogManagementSystem.LogManager.Security;

import lombok.*;

@Getter @Setter
public class RegisterRequest {
    private String email;
    private String password;
    private String companyName;
}
