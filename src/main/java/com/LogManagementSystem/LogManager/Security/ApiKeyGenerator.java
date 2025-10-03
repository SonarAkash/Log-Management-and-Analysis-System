package com.LogManagementSystem.LogManager.Security;


import com.LogManagementSystem.LogManager.Entity.Tenant;
import com.LogManagementSystem.LogManager.Entity.User;
import com.LogManagementSystem.LogManager.Repository.TenantRepository;
import com.LogManagementSystem.LogManager.Repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Controller
public class ApiKeyGenerator {

    private final AuthService authService;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final Logger logger = LoggerFactory.getLogger(ApiKeyGenerator.class);

    @Autowired
    public ApiKeyGenerator(AuthService authService, TenantRepository tenantRepository, UserRepository userRepository) {
        this.authService = authService;
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("new/api-key")
    @Transactional
    public ResponseEntity<?> apiKeyGenerator(Authentication authentication){
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated.");
        }
        try {
            User userDetails = (User) authentication.getPrincipal();
            User hibernateSessionUser = userRepository.findByEmail(userDetails.getEmail()).orElse(null);
            if(hibernateSessionUser == null) {
                logger.info("User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));
            }
            Tenant tenant = hibernateSessionUser.getTenant();
            if (tenant == null) {
                logger.info("Tenant not found for user: {}", userDetails.getEmail());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Tenant not found"));
            }
            UUID tenantId = tenant.getId();
            String newApiKey =  authService.getTenantToken(tenantId);
            int result = tenantRepository.updateApiToken(tenantId, newApiKey);
            Map<String, String> body = new HashMap<>();
            if(result > 0){
                body.put("apiKey", newApiKey);
                return new ResponseEntity<>(body, HttpStatus.CREATED);
            }
            body.put("error", "Failed to generate new api-key");
            return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            Map<String, String> body = new HashMap<>();
            body.put("error", "Failed to generate new api-key");
            logger.info("Failed to generate new Token : ", e);
            return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("api-key")
    public ResponseEntity<?> showApiKey(Authentication authentication){
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated.");
        }
        try {
            User userDetails = (User) authentication.getPrincipal();
            User hibernateSessionUser = userRepository.findByEmail(userDetails.getEmail()).orElse(null);
            if(hibernateSessionUser == null) {
                logger.info("User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));
            }

            Tenant tenant = hibernateSessionUser.getTenant();
            if (tenant == null) {
                logger.info("Tenant not found for user: {}", userDetails.getEmail());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Tenant not found"));
            }

            String apiKey = tenant.getApiTokenHash();
            if (apiKey == null || apiKey.isEmpty()) {
                logger.info("No API key found for tenant of user: {}", userDetails.getEmail());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "No API key found"));
            }
            Map<String, String> body = new HashMap<>();
            body.put("apiKey", apiKey);
            return new ResponseEntity<>(body, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> body = new HashMap<>();
            body.put("error", "Failed to view api-key");
            logger.info("Failed to view api-key : ", e);
            return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
