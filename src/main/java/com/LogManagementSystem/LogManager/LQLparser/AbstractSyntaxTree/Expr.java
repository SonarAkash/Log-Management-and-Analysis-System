package com.LogManagementSystem.LogManager.LQLparser.AbstractSyntaxTree;


/**
 * Base interface for all expression nodes in the AST.
 * This uses the Visitor pattern, which allows us to add new operations
 * (like SQL generation or semantic analysis) without changing the node classes.
 * <R> The return type of the visitor's operation.
 */
public interface Expr {
    interface Visitor<R> {
        R visitBinaryExpr(Binary expr);
        R visitGroupingExpr(Grouping expr);
        R visitLiteralExpr(Literal expr);
        R visitUnaryExpr(Unary expr);
    }

    <R> R accept(Visitor<R> visitor);
}
