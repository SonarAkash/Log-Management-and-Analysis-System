package com.LogManagementSystem.LogManager.Security.Exception;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler{

    @ExceptionHandler(InvalidEmailException.class)
    public ResponseEntity<?> handleInvalidEmailException(InvalidEmailException exception, WebRequest request){
        System.err.println("Invalid email format : " + exception.getEmail());
        Map<String, String> body = new HashMap<>();
        body.put("error", exception.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EmailInUserException.class)
    public ResponseEntity<?> handleEmailInUserException(EmailInUserException exception, WebRequest request){
        System.err.println("Email already in use : " + exception.getEmail());
        Map<String, String> body = new HashMap<>();
        body.put("error", exception.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
}
