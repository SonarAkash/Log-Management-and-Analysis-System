package com.LogManagementSystem.LogManager.Security;

import com.LogManagementSystem.LogManager.Entity.Role;
import com.LogManagementSystem.LogManager.Entity.Tenant;
import com.LogManagementSystem.LogManager.Entity.User;
import com.LogManagementSystem.LogManager.Repository.TenantRepository;
import com.LogManagementSystem.LogManager.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TenantRepository tenantRepository;

    public AuthenticationResponse register(RegisterRequest request){
        Tenant tenant = tenantRepository.findByCompanyName(request.getCompanyName())
                .orElseGet(() -> {
                    UUID uuid = UUID.randomUUID();
                    Tenant newTenant = Tenant.builder()
                            .id(uuid)
                            .companyName(request.getCompanyName())
                            .apiTokenHash(generateTenantToken(uuid))
                            .createdAt(Instant.now())
                            .build();
                    return tenantRepository.save(newTenant);
                });
        var user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
//                .role(Role.ADMIN)
                .role(tenantRepository.existsByCompanyName(request.getCompanyName()) ? Role.ADMIN : Role.USER)
                .tenant(tenant)
                .createdAt(Instant.now())
                .build();

        userRepository.save(user);
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .apiKey(tenant.getApiTokenHash())
                .build();
    }


    private String generateTenantToken(UUID tenantId) {
        try {
            // random salt
            byte[] salt = new byte[16];
            SecureRandom random = new SecureRandom();
            random.nextBytes(salt);

            // tenantId with salt
            String input = String.valueOf(tenantId) + ":" + Base64.getEncoder().encodeToString(salt);

            // Hash using SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            // Encode the hash as Base64 string
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

}
