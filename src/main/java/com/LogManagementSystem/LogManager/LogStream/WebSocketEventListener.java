package com.LogManagementSystem.LogManager.LogStream;

import com.LogManagementSystem.LogManager.Entity.User;
import com.LogManagementSystem.LogManager.Repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.Optional;
import java.util.UUID;

@Component
public class WebSocketEventListener {

    private final ActiveClient activeClient;
    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    public WebSocketEventListener(ActiveClient activeClient, UserRepository userRepository) {
        this.activeClient = activeClient;
        this.userRepository = userRepository;
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event){
        StompHeaderAccessor headers = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = headers.getUser();
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
        Principal principal = headers.getUser();
        String sessionId = headers.getSessionId();
        CloseStatus closeStatus = event.getCloseStatus();
        logger.warn("WebSocket session disconnected: SessionId='{}',  CloseCode={}, Reason='{}'",
                sessionId,
                closeStatus.getCode(),
                closeStatus.getReason());
        Optional<User> userOptional = userRepository.findByEmail(principal.getName());
        if(userOptional.isPresent()){
            UUID tenantId = userOptional.get().getTenant().getId();
            activeClient.unregisterClient(tenantId);
        }
    }
}
