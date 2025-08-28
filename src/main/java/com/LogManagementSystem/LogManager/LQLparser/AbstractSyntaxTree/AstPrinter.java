package com.LogManagementSystem.LogManager.LQLparser.AbstractSyntaxTree;


/**
 * A utility class to print the AST in a readable, LISP-like format.
 * This class implements the Visitor pattern defined in the Expr interface.
 */
public class AstPrinter implements Expr.Visitor<String> {

    public String print(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public String visitBinaryExpr(Binary expr) {
        return parenthesize(expr.operator().lexeme(), expr.left(), expr.right());
    }

    @Override
    public String visitGroupingExpr(Grouping expr) {
        return parenthesize("group", expr.expression());
    }

    @Override
    public String visitLiteralExpr(Literal expr) {
        return expr.value().lexeme();
    }

    @Override
    public String visitUnaryExpr(Unary expr) {
        return parenthesize(expr.operator().lexeme(), expr.right());
    }

    private String parenthesize(String name, Expr... exprs) {
        StringBuilder builder = new StringBuilder();
        builder.append("(").append(name.toLowerCase());
        for (Expr expr : exprs) {
            builder.append(" ");
            builder.append(expr.accept(this));
        }
        builder.append(")");
        return builder.toString();
    }
}