package com.LogManagementSystem.LogManager.LQLparser.AbstractSyntaxTree;


import com.LogManagementSystem.LogManager.LQLparser.Token.Token;

/**
 * Represents a literal value, which is a leaf node in the AST.
 * This can be a KEY_VALUE pair or a BAREWORD.
 */
public record Literal(Token value) implements Expr {
    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitLiteralExpr(this);
    }
}
