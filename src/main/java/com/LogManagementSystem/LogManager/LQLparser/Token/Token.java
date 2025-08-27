package com.LogManagementSystem.LogManager.LQLparser.Token;

public record Token(TokenType type, String lexeme, int position) {
    @Override
    public String toString() {
        return "Token{" +
                "type=" + type +
                ", lexeme='" + lexeme + '\'' +
                ", position=" + position +
                '}';
    }
}
