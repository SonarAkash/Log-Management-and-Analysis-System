package com.LogManagementSystem.LogManager.LQLparser.AbstractSyntaxTree;

import com.LogManagementSystem.LogManager.LQLparser.Token.Token;

/**
 * Represents a binary operation with a left operand, an operator, and a right operand.
 * Examples: A AND B, C OR D
 */
public record Binary(Expr left, Token operator, Expr right) implements Expr {
    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitBinaryExpr(this);
    }
}