package com.LogManagementSystem.LogManager.Security;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@Valid @RequestBody RegisterRequest request){
        try {
            return ResponseEntity.ok(authService.register(request));
        } catch (Exception e) {
            System.err.println("Registration failed : " + e.getMessage());
            return ResponseEntity.
                    status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AuthenticationResponse.builder()
                    .error("Error registering, please try again later ! " + e)
                    .build()
            );
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody AuthenticationRequest request){
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            final var user = userDetailsService.loadUserByUsername(request.getEmail());
            final var jwt = jwtService.generateToken(user);
            System.out.println("Found user");
            return ResponseEntity.ok(AuthenticationResponse.builder()
                    .token(jwt)
                    .build());
        } catch (Exception e) {
            System.err.println("user not found");
            return ResponseEntity.
                    status(HttpStatus.UNAUTHORIZED)
                    .body(AuthenticationResponse.builder()
                            .error("Unauthorized access ! ")
                            .build());
        }

    }
}
