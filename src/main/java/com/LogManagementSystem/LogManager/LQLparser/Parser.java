package com.LogManagementSystem.LogManager.LQLparser;


import com.LogManagementSystem.LogManager.LQLparser.AbstractSyntaxTree.Binary;
import com.LogManagementSystem.LogManager.LQLparser.AbstractSyntaxTree.Expr;
import com.LogManagementSystem.LogManager.LQLparser.AbstractSyntaxTree.Grouping;
import com.LogManagementSystem.LogManager.LQLparser.AbstractSyntaxTree.Literal;
import com.LogManagementSystem.LogManager.LQLparser.AbstractSyntaxTree.Unary;
import com.LogManagementSystem.LogManager.LQLparser.Exception.ParserError;
import com.LogManagementSystem.LogManager.LQLparser.Token.Token;
import com.LogManagementSystem.LogManager.LQLparser.Token.TokenType;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;


import java.util.List;


/**
 * The Parser consumes a list of tokens and produces an Abstract Syntax Tree (AST).
 * It implements a recursive descent parser to handle the grammar rules and operator precedence.
 */

@Service
@Scope("prototype")
public class Parser {

//    private static class ParseError extends RuntimeException {}

    private List<Token> tokens;
    private int current = 0;

    public void init(List<Token> tokens) {
        this.tokens = tokens;
    }

    public Expr parse() {
//        try {
//            if (isAtEnd()) return null;
//            return expression();
//        } catch (ParserError error) {
//            return null;
//        }
        if (isAtEnd()) return null;
        return expression();
    }

    //  Grammar Implementation: expression -> term -> factor -> unary -> primary

    // An expression is a series of terms separated by OR.
    private Expr expression() {
        Expr expr = term();
        while (match(TokenType.OR)) {
            Token operator = previous();
            Expr right = term();
            expr = new Binary(expr, operator, right);
        }
        return expr;
    }

    // A term is a series of factors separated by AND.
    private Expr term() {
        Expr expr = factor();
        while (match(TokenType.AND)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Binary(expr, operator, right);
        }
        return expr;
    }

    // A factor is a series of primaries separated by an implicit AND.
    private Expr factor() {
        Expr expr = unary();
        while (isAtPrimary()) {
            Token implicitAnd = new Token(TokenType.AND, "AND (implicit)", peek().position());
            Expr right = unary();
            expr = new Binary(expr, implicitAnd, right);
        }
        return expr;
    }

    // A unary handles NOT operators.
    private Expr unary() {
        if (match(TokenType.NOT)) {
            Token operator = previous();
            Expr right = unary();
            return new Unary(operator, right);
        }
        return primary();
    }

    // A primary is the highest-precedence expression: literals or groupings.
    private Expr primary() {
        if (match(TokenType.KEY_VALUE, TokenType.BAREWORD)) {
            return new Literal(previous());
        }
        if (match(TokenType.LPAREN)) {
            Expr expr = expression(); // Recursively parse the inner expression
            consume(TokenType.RPAREN, "Expect ')' after expression.");
            return new Grouping(expr);
        }
        throw error(peek(), "Expect expression.");
    }

    //  Helper Methods

    private boolean isAtPrimary() {
        if (isAtEnd()) return false;
        TokenType type = peek().type();
        // A primary can start a new implicit AND group.
        // But, it cannot be an operator like (AND/OR) or a closing paren.
        return type == TokenType.BAREWORD || type == TokenType.KEY_VALUE || type == TokenType.LPAREN || type == TokenType.NOT;
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        throw error(peek(), message);
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type() == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type() == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private ParserError error(Token token, String message) {
        System.err.println("Parse Error at token " + token + ": " + message);
        return new ParserError("Parse Error at token " + token + ": " + message);
    }
}


