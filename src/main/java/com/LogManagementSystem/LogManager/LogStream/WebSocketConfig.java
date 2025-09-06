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
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

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
                .setTaskScheduler(messageBrokerTaskScheduler());
//                .setHeartbeatValue(new long[]{10000, 10000});

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

    /**
     *
     *
     * This below method might look obsolete because anyway jwt filter is already authenticating
     *      any user, but the problem is when user is authenticated by jwt filter the PRINCIPAL
     *      object is not transferred to websocket protocol automatically. Since websocket is stateful
     *      and http is stateless, the jwt filter don't have any knowledge of things happening in websocket
     *      protocol (http was upgraded to socket later). So in order to authenticate user and save their
     *      Principal object for websocket session, the below method is required for that purpose.
     *      This is needed otherwise the system closes the session for some
     *      reason which I don't know.
     */

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
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

    /**
     * This part is the needed for better performance, it increases the size of message
     * that client can send. If client send a message which exceed the capacity then
     * server will close the connection. So these changes are necessary.
     */
    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        // Increase the maximum allowed message size to 256KB (default is 64KB)
        registration.setMessageSizeLimit(256 * 1024);

        // Increase other related buffer limits for better performance under load
        registration.setSendBufferSizeLimit(1024 * 1024); // 1MB
        registration.setSendTimeLimit(20000); // 20 seconds
    }
}
