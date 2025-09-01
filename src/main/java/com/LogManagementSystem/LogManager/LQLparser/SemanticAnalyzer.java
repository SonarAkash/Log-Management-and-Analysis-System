package com.LogManagementSystem.LogManager.LQLparser;

import com.LogManagementSystem.LogManager.LQLparser.AbstractSyntaxTree.*;
import com.LogManagementSystem.LogManager.LQLparser.Exception.SemanticException;
import com.LogManagementSystem.LogManager.LQLparser.Token.Token;
import com.LogManagementSystem.LogManager.LQLparser.Token.TokenType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class SemanticAnalyzer implements Expr.Visitor<Void>{

    public void analyze(Expr expression) {
        if (expression == null) {
            // This can happen if the parser failed.
            return;
        }
        try {
            // This starts the traversal of the entire AST.
            expression.accept(this);
        } catch (SemanticException e) {
            //Later in future may be handel it more gracefully.
            throw e;
        }
    }

    @Override
    public Void visitBinaryExpr(Binary expr) {
        // First, recursively analyze the left and right sides of the expression.
        expr.left().accept(this);
        expr.right().accept(this);

        // After visiting the children, analyze the binary node itself.
        // For example, this is where I check for `key:A AND key:B`.
        if (expr.operator().type() == TokenType.AND) {
            validateAndChain(expr);
        }

        return null; // Visitor methods for Void must return null.
    }

    @Override
    public Void visitGroupingExpr(Grouping expr) {
        // For a grouping, simply traverse into the nested expression.
        expr.expression().accept(this);
        return null;
    }


    /**
     * Traverses a chain of AND expressions, collects all KEY_VALUE literals,
     * and checks for duplicates.
     * @param expr The top-level AND expression in a potential chain.
     */
    private void validateAndChain(Binary expr) {
        List<Token> keyValueTokens = new ArrayList<>();
        collectKeyValueTokensInAndChain(expr, keyValueTokens);

        Set<String> keys = new HashSet<>();
        for (Token token : keyValueTokens) {
            String key = extractKeyFromToken(token);
            if (key != null) {
                if (!keys.add(key)) {
                    // Found a duplicate key!
                    throw new SemanticException(token,
                            "Cannot use AND to search for multiple values in the same field ('" + key + "').");
                }
            }
        }
    }

    /**
     * Recursively navigates down a left-associative AND chain to collect all KEY_VALUE tokens.
     * @param expr The current expression in the chain.
     * @param tokens The list to add found tokens to.
     */
    private void collectKeyValueTokensInAndChain(Expr expr, List<Token> tokens) {
        if (expr instanceof Binary binaryExpr && binaryExpr.operator().type() == TokenType.AND) {
            // It's another AND, so recurse down the left side.
            collectKeyValueTokensInAndChain(binaryExpr.left(), tokens);
            collectKeyValueTokensInAndChain(binaryExpr.right(), tokens);
        } else if (expr instanceof Literal literalExpr && literalExpr.value().type() == TokenType.KEY_VALUE) {
            // It's a key-value literal, add it to our list.
            tokens.add(literalExpr.value());
        }
        // Ignore other expression types (like Groups or ORs) in this specific check.
    }


    /**
     * Method to parse the key from a KEY_VALUE token's lexeme.
     * e.g., "level:error" -> "level"
     * @param token The KEY_VALUE token.
     * @return The key part of the token, or null if it can't be parsed.
     */
    private String extractKeyFromToken(Token token) {
        if (token.type() != TokenType.KEY_VALUE) {
            return null;
        }
        String lexeme = token.lexeme();
        int separatorIndex = lexeme.indexOf(':');
        if (separatorIndex == -1) {
            separatorIndex = lexeme.indexOf('=');
        }
        if (separatorIndex != -1) {
            return lexeme.substring(0, separatorIndex).trim();
        }
        return null;
    }

    @Override
    public Void visitLiteralExpr(Literal expr) {
        // Literals (leaves of the tree) have no children to traverse.
        // Could add validation here if needed (e.g., check if a value is too long).
        return null;
    }

    @Override
    public Void visitUnaryExpr(Unary expr) {
        // For a unary expression, we traverse into its single operand.
        expr.right().accept(this);
        return null;
    }
}
