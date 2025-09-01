package com.LogManagementSystem.LogManager.QueryAPI;

import com.LogManagementSystem.LogManager.Entity.LogEvent;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.UUID;

@Controller
public class LogSearchController {

    @Autowired
    private QueryService queryService;

    @GetMapping("logs/search")
    public ResponseEntity<?> searchLogs(@Valid @RequestBody QueryDTO queryDTO, @RequestParam int offset, @RequestParam int size){
        System.out.println("HI");
        UUID tenantId =  UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11");
        try{
            Page<LogEvent> page = queryService.getLogs(offset, size, queryDTO.query(), tenantId);
            if(page != null){
                return ResponseEntity.ok(page);
            }else{
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "bad request !"));
            }
        } catch (Exception e){
            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }
}
