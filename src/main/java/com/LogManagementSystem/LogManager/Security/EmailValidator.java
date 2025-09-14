package com.LogManagementSystem.LogManager.Security;

import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

@Service
public class EmailValidator {
    private static final String EMAIL_REGEX =
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9][A-Za-z0-9.-]*\\.[A-Za-z]{2,}$";

    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    public boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        // Remove any trailing dots
        email = email.trim();
        if (email.endsWith(".")) {
            return false;
        }

        // Check for consecutive dots
        if (email.contains("..")) {
            return false;
        }

        // Check if there's a dot before @
        int atIndex = email.indexOf('@');
        if (atIndex > 0 && email.charAt(atIndex - 1) == '.') {
            return false;
        }

        return EMAIL_PATTERN.matcher(email).matches();
    }
}