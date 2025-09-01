package com.LogManagementSystem.LogManager.LQLparser.Exception;


import com.LogManagementSystem.LogManager.LQLparser.Token.Token;

/**
 * A custom exception thrown by the SemanticAnalyzer when a query is
 * syntactically valid but logically nonsensical.
 */
public class SemanticException extends RuntimeException {

    public SemanticException(Token token, String message) {
        // Include the token to report the error's location.
        super("Semantic Error at '" + token.lexeme() + "' (position " + token.position() + "): " + message);
    }

    public SemanticException(String message) {
        super(message);
    }
}

