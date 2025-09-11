package com.LogManagementSystem.LogManager.LQLparser.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalParsingExceptionHandler {
    @ExceptionHandler(SemanticException.class)
    public ResponseEntity<?> handleSemanticException(WebRequest request, SemanticException exception){
        System.err.println("Invalid email format : " + exception.getMessage());
        Map<String, String> body = new HashMap<>();
        body.put("error", exception.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ParserError.class)
    public ResponseEntity<?> handleParserError(WebRequest request, ParserError error){
        System.err.println(error.getMessage());
        Map<String, String> body = new HashMap<>();
        body.put("error", error.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
}
