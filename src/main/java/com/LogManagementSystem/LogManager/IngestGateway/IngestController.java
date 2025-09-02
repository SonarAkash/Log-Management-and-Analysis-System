package com.LogManagementSystem.LogManager.IngestGateway;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
public class IngestController {

    private final FileWalAppender fileWalAppender;

    public IngestController(FileWalAppender fileWalAppender) {
        this.fileWalAppender = fileWalAppender;
    }

    @GetMapping("actuator/health")
    public String hello(){
        return "Hello !";
    }

    @PostMapping("api/v1/ingest")
    public ResponseEntity<?> incomingLog(HttpServletRequest request,
                                            @RequestBody String rawLog){
        String timestamp = String.valueOf(Instant.now());
        UUID tenantId =  (UUID) request.getAttribute("tenantId");
//        System.out.println(request.getAttribute("tenantId"));
        rawLog = "[" + tenantId + "][" + timestamp + "]"+ rawLog;
        boolean result = fileWalAppender.write(rawLog);
        return result ? ResponseEntity.status(HttpStatus.OK).build()
                : ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }
}
