package com.LogManagementSystem.LogManager.Security;

import jakarta.validation.constraints.Email;
import lombok.*;

@Getter @Setter
public class RegisterRequest {
    private String email;
    private String password;
    private String companyName;
}
