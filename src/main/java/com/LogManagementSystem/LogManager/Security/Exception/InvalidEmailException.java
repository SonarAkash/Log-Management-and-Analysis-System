package com.LogManagementSystem.LogManager.Security.Exception;


import lombok.Getter;

@Getter
public class InvalidEmailException extends RuntimeException{
    private final String email;

    public InvalidEmailException(String email){
        super("Invalid email format");
        this.email = email;
    }
}
