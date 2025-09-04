package com.LogManagementSystem.LogManager.LogStream;

import com.LogManagementSystem.LogManager.Entity.User;
import com.LogManagementSystem.LogManager.Repository.UserRepository;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.Optional;
import java.util.UUID;

@Component
public class WebSocketEventListener {

    private final ActiveClient activeClient;
    private final UserRepository userRepository;

    public WebSocketEventListener(ActiveClient activeClient, UserRepository userRepository) {
        this.activeClient = activeClient;
        this.userRepository = userRepository;
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event){
        StompHeaderAccessor headers = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = headers.getUser();;
        String sessionId = headers.getSessionId();
        if(principal != null){
            Optional<User> userOptional = userRepository.findByEmail(principal.getName());
            if(userOptional.isPresent()){
                UUID tenantId = userOptional.get().getTenant().getId();
                activeClient.registerClient(tenantId, userOptional.get().getEmail());
            }
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event){
        StompHeaderAccessor headers = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = headers.getUser();;
        String sessionId = headers.getSessionId();
        if(principal != null){
            Optional<User> userOptional = userRepository.findByEmail(principal.getName());
            if(userOptional.isPresent()){
                UUID tenantId = userOptional.get().getTenant().getId();
                activeClient.unregisterClient(tenantId);
            }
        }
    }
}
