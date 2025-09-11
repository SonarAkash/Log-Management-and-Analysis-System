package com.LogManagementSystem.LogManager.Security.Exception;

import lombok.Getter;

@Getter
public class EmailInUserException extends RuntimeException{
    private final String email;

    public EmailInUserException(String email){
        super("Email already in use");
        this.email = email;
    }

}
