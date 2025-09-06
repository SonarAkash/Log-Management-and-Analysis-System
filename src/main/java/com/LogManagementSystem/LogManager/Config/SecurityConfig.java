package com.LogManagementSystem.LogManager.Config;

import com.LogManagementSystem.LogManager.Security.JwtAuthenticationFilter;
import com.LogManagementSystem.LogManager.Security.TenantApiKeyFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Configuration
public class SecurityConfig {


    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final TenantApiKeyFilter tenantApiKeyFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, TenantApiKeyFilter tenantApiKeyFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.tenantApiKeyFilter = tenantApiKeyFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/auth/**", "/public", "/actuator/**", "/",
                                "/*.html",
                                "/*.css",
                                "/*.js",
                                "/favicon.ico",
                                "/websocket-connect/**" ).permitAll()
                        //only temporary
//                        .requestMatchers("/subscribe-stream").authenticated()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(tenantApiKeyFilter, JwtAuthenticationFilter.class)


                .exceptionHandling(ex -> ex

                        // Handles failed authentication attempts (e.g., bad token) -> 401
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
                        // Handles failed authorization attempts (e.g., wrong role) -> 403
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json");

                            // Creating a clear JSON error response
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
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
