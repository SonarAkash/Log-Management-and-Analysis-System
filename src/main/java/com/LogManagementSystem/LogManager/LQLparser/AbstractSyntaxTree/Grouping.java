package com.LogManagementSystem.LogManager.LQLparser.AbstractSyntaxTree;

/**
 * Represents a grouping of expressions, corresponding to parentheses.
 * Example: (A AND B)
 */
public record Grouping(Expr expression) implements Expr {
    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitGroupingExpr(this);
    }
}