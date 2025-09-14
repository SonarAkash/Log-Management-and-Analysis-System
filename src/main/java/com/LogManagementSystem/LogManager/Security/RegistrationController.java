package com.LogManagementSystem.LogManager.Security;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class RegistrationController {

    @Autowired
    private OtpService otpService;

    @Autowired
    private AuthService authService;

    @Autowired
    private EmailValidator emailValidator;

    @PostMapping("/register/initiate")
    public ResponseEntity<?> initiateRegistration(@RequestParam String email) {
        try {
            // Validate email format first
            if (!emailValidator.isValidEmail(email)) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "Invalid email format");
                return ResponseEntity.badRequest().body(response);
            }
            
            otpService.generateAndSendOTP(email);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Please check your email for OTP verification code");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Registration initiation failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/register/complete")
    public ResponseEntity<?> completeRegistration(
            @RequestParam String email,
            @RequestParam String otp,
            @RequestBody RegisterRequest registerRequest) {
        try {
            if (!email.equals(registerRequest.getEmail())) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "Email mismatch");
                return ResponseEntity.badRequest().body(response);
            }

            if (!otpService.verifyOTP(email, otp)) {
                Map<String, String> response = new HashMap<>();
                String savedOtp = otpService.getOTP(email);
                if (savedOtp == null) {
                    response.put("error", "OTP expired, please request a new one");
                } else {
                    response.put("error", "Invalid OTP");
                }
                return ResponseEntity.badRequest().body(response);
            }

            return ResponseEntity.ok(authService.register(registerRequest));

        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Registration failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/register/resend-otp")
    public ResponseEntity<?> resendOtp(@RequestParam String email) {
        return initiateRegistration(email);
    }
}