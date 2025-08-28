package com.LogManagementSystem.LogManager.LQLparser.AbstractSyntaxTree;

import com.LogManagementSystem.LogManager.LQLparser.Token.Token;

/**
 * Represents a unary operation with an operator and a right-hand operand.
 * Example: NOT A
 */
public record Unary(Token operator, Expr right) implements Expr {
    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitUnaryExpr(this);
    }
}
