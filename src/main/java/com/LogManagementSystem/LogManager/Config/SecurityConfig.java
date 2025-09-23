package com.LogManagementSystem.LogManager.Config;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.LogManagementSystem.LogManager.Security.JwtAuthenticationFilter;
import com.LogManagementSystem.LogManager.Security.TenantApiKeyFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final TenantApiKeyFilter tenantApiKeyFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          TenantApiKeyFilter tenantApiKeyFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.tenantApiKeyFilter = tenantApiKeyFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/auth/**",
                                "/public/**",
                                "/actuator/**",
                                "/*.html",
                                "/test.html",
                                "/*.css",
                                "/*.js",
                                "/favicon.ico",
                                "/websocket-connect/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/auth/otp/**",
                                "/api/auth/**",
                                "/api/auth/register/**",
                                "/api/auth/register/initiate/**",
                                "/api/auth/register/complete/**",
                                "/static/**",
                                "/test-resource"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(tenantApiKeyFilter, JwtAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        // Handles failed authentication attempts (401)
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            Map<String, Object> data = new HashMap<>();
                            data.put("timestamp", new Date().toString());
                            data.put("status", HttpServletResponse.SC_UNAUTHORIZED);
                            data.put("error", "Unauthorized");
                            data.put("message", "Authentication Failed: " + authException.getMessage());
                            data.put("path", request.getRequestURI());
                            new ObjectMapper().writeValue(response.getWriter(), data);
                        })
                        // Handles failed authorization attempts (403)
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json");

                            Map<String, Object> data = new HashMap<>();
                            data.put("timestamp", new Date().toString());
                            data.put("status", HttpServletResponse.SC_FORBIDDEN);
                            data.put("error", "Forbidden");
                            data.put("message", "Access Denied: You do not have the necessary permissions.");
                            data.put("path", request.getRequestURI());

                            ObjectMapper objectMapper = new ObjectMapper();
                            response.getWriter().write(objectMapper.writeValueAsString(data));
                        })
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Value("${cors.allowed-origins}") // <-- Add this field to read from properties
    private String allowedOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                allowedOrigins,
                "http://localhost:8080" // For local development
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
