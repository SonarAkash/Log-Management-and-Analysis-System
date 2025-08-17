package com.LogManagementSystem.LogManager.IngestGateway;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IngestController {

    private final FileWalAppender fileWalAppender;

    public IngestController(FileWalAppender fileWalAppender) {
        this.fileWalAppender = fileWalAppender;
    }

    @PostMapping("api/v1/ingest")
    public ResponseEntity<String> incomingLog(@RequestBody RawDTO log){
        String uuid = fileWalAppender.appendLog(log);
        if(uuid != null){
            return ResponseEntity.ok("Success !");
        }
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("try again later !");
    }
}
