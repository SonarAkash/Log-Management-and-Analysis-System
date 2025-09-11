package com.LogManagementSystem.LogManager.QueryAPI;

import com.LogManagementSystem.LogManager.Entity.LogEvent;

import com.LogManagementSystem.LogManager.Entity.User;
import com.LogManagementSystem.LogManager.Repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Controller
public class LogSearchController {

    @Autowired
    private QueryService queryService;
    @Autowired
    private UserRepository userRepository;

    @PostMapping("logs/search")
    public ResponseEntity<?> searchLogs(@Valid @RequestBody SearchRequestDTO queryDTO, Pageable pageable){
//        System.out.println("HI");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Optional<User> userOptional = userRepository.findByEmail(userDetails.getUsername());
        if(userOptional.isEmpty()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "error","Unauthorized Request, please login in "));
        }
        UUID tenantId =  userOptional.get().getTenant().getId();
        try{
            Page<LogEvent> page = queryService.getLogs(pageable, queryDTO, tenantId);
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
