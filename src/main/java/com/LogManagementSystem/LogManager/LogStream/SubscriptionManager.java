package com.LogManagementSystem.LogManager.LogStream;

import com.LogManagementSystem.LogManager.Entity.User;
import com.LogManagementSystem.LogManager.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

@RestController
public class SubscriptionManager {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ActiveClient activeClient;

    @PostMapping("/subscribe-stream")
    public ResponseEntity<?> subscribe(Authentication authentication){
        User user = (User) authentication.getPrincipal();
        Optional<User> userOptional = userRepository.findByEmail(user.getEmail());
        if(userOptional.isEmpty()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UUID tenantId = userOptional.get().getTenant().getId();
        activeClient.registerClient(tenantId, user.getEmail());
        return ResponseEntity.accepted().body(tenantId.toString());
    }
}
