package com.LogManagementSystem.LogManager.LogStream;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class ActiveClient {
    private final Map<UUID, String> clientToSessionId = new HashMap<>();

    public void registerClient(UUID tenantId, String email){
//        System.out.println("user register : " + tenantId + " , " + email);
        clientToSessionId.put(tenantId, email);
    }
    public boolean containsClient(UUID tenantId){
        return clientToSessionId.containsKey(tenantId);
    }
    public void unregisterClient(UUID tenantId){
        clientToSessionId.remove(tenantId);
    }
    public String getClientEmail(UUID tenantId){
        return clientToSessionId.getOrDefault(tenantId, "");
    }
}
