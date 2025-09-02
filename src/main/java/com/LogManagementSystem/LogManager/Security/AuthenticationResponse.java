package com.LogManagementSystem.LogManager.Security;

import lombok.*;

@Data
@Builder
@Setter @Getter
public class AuthenticationResponse {
    private String token;
    private String error;
    private String apiKey;
}
