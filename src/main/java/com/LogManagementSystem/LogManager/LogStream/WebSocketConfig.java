package com.LogManagementSystem.LogManager.LogStream;


import com.LogManagementSystem.LogManager.Security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.DefaultManagedTaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry){

        registry.enableSimpleBroker("/queue/stream")
                .setTaskScheduler(messageBrokerTaskScheduler())
                .setHeartbeatValue(new long[]{10000, 10000});

        registry.setUserDestinationPrefix("/user");

        registry.setApplicationDestinationPrefixes("/subscribe-stream");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry){
        registry.addEndpoint("websocket-connect").withSockJS();
    }

    @Bean
    public TaskScheduler messageBrokerTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("ws-msg-broker-");
        scheduler.setDaemon(true);
        return scheduler;
    }


    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                // Check if it's a CONNECT message
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    // Get the Authorization header
                    String authHeader = accessor.getFirstNativeHeader("Authorization");
                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        String jwt = authHeader.substring(7);
                        String userEmail = jwtService.extractUsername(jwt);

                        if (userEmail != null) {
                            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
                            if (jwtService.isTokenValid(jwt, userDetails)) {
                                UsernamePasswordAuthenticationToken authToken =
                                        new UsernamePasswordAuthenticationToken(
                                                userDetails, null, userDetails.getAuthorities());
                                // Set the user for this session
                                accessor.setUser(authToken);
                            }
                        }
                    }
                }
                return message;
            }
        });
    }
}
