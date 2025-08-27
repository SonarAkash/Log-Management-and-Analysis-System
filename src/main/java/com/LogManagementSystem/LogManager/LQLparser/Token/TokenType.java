package com.LogManagementSystem.LogManager.LQLparser.Token;

public enum TokenType {
    LPAREN,     // (
    RPAREN,      // )

    // operators
    AND,
    OR,
    NOT,

    KEY_VALUE,  // e.g. level:error or status=500

    BAREWORD,   // for full text search

    EOF     // represents the end of the string
}
