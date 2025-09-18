package com.LogManagementSystem.LogManager.Security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    private final JavaMailSender mailSender;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtpEmail(String toEmail, String otp) throws MessagingException {
        MimeMessage message = null;
        try {
            message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom("akashsonar.9113@gmail.com");
            helper.setTo(toEmail);
            helper.setSubject("LogFlux - Your Verification Code");

        String htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        line-height: 1.6;
                        margin: 0;
                        padding: 0;
                        background-color: #f4f4f4;
                    }
                    .container {
                        max-width: 600px;
                        margin: 20px auto;
                        background-color: #ffffff;
                        border-radius: 10px;
                        box-shadow: 0 2px 5px rgba(0,0,0,0.1);
                        padding: 20px;
                    }
                    .header {
                        text-align: center;
                        padding: 20px 0;
                        border-bottom: 2px solid #0066cc;
                        margin-bottom: 20px;
                    }
                    .logo {
                        font-size: 36px;
                        font-weight: bold;
                        color: #0066cc;
                        margin: 0;
                        text-shadow: 1px 1px 2px rgba(0,0,0,0.1);
                    }
                    .tagline {
                        color: #666;
                        font-style: italic;
                        margin-top: 5px;
                    }
                    .otp-container {
                        background: linear-gradient(145deg, #f8f9fa 0%, #e9ecef 100%);
                        border-radius: 8px;
                        padding: 25px;
                        text-align: center;
                        margin: 25px 0;
                        border: 1px solid #dee2e6;
                    }
                    .otp-code {
                        font-size: 38px;
                        letter-spacing: 8px;
                        color: #0066cc;
                        font-weight: bold;
                        margin: 15px 0;
                        text-shadow: 1px 1px 1px rgba(0,0,0,0.1);
                        background: #ffffff;
                        padding: 10px;
                        border-radius: 5px;
                        display: inline-block;
                    }
                    .expiry-text {
                        color: #dc3545;
                        font-size: 14px;
                        margin-top: 10px;
                        font-weight: bold;
                    }
                    .message {
                        color: #495057;
                        padding: 0 20px;
                        line-height: 1.8;
                    }
                    .footer {
                        text-align: center;
                        padding: 20px 0;
                        color: #666;
                        font-size: 12px;
                        border-top: 1px solid #eee;
                        margin-top: 20px;
                    }
                    .footer a {
                        color: #0066cc;
                        text-decoration: none;
                    }
                    .footer a:hover {
                        text-decoration: underline;
                    }
                    .security-note {
                        font-size: 13px;
                        color: #666;
                        font-style: italic;
                        margin-top: 15px;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1 class="logo">LogFlux</h1>
                        <p class="tagline">Log Management & Analysis Platform</p>
                    </div>
                    
                    <div class="message">
                        <p>Hello,</p>
                        <p>Thank you for choosing LogFlux for your log management needs. To ensure the security of your account, please use the verification code below to complete your registration:</p>
                        
                            <div class="otp-container">
                            <div class="otp-code">{0}</div>
                            <p class="expiry-text">⏰ This code will expire in 1 minute</p>
                        </div>                        <p class="security-note">If you didn't request this code, please ignore this email. Your security is important to us, and someone might have typed your email address by mistake.</p>
                    </div>
                    
                    <div class="footer">
                        <p>This is an automated message, please do not reply.</p>
                        <p>&copy; 2025 LogFlux. All rights reserved.</p>
                        <p>
                            <a href="#">Help Center</a> • 
                            <a href="#">Privacy Policy</a> • 
                            <a href="#">Terms of Service</a>
                        </p>
                    </div>
                </div>
            </body>
            </html>
        """;

        // Replace the placeholder with the actual OTP
        htmlContent = htmlContent.replace("{0}", otp);
        helper.setText(htmlContent, true);
            mailSender.send(message);
            logger.info("OTP email sent successfully to: {}", maskEmail(toEmail));
            
        } catch (Exception e) {
            logger.error("Failed to send OTP email to {}: {}", maskEmail(toEmail), e.getMessage());
            throw new MessagingException("Failed to send OTP email", e);
        }
    }

    // Utility method to mask email for logging
    private String maskEmail(String email) {
        if (email == null || email.length() < 5) return "***";
        int atIndex = email.indexOf('@');
        if (atIndex <= 2) return "***" + email.substring(atIndex);
        return email.substring(0, 2) + "***" + email.substring(atIndex);
    }
}