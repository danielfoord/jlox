package com.danielfoord.lox.expressions;

import com.danielfoord.lox.Token;

import java.util.List;

public class CallExpr extends Expr {
    public final Expr callee;
    public final Token paren; // Used for error reporting
    public final List<Expr> arguments;

    public CallExpr(Expr callee, Token paren, List<Expr> arguments) {
        this.callee = callee;
        this.paren = paren;
        this.arguments = arguments;
    }

    @Override
    public <R> R accept(ExprVisitor<R> visitor) {
        return visitor.visitCallExpr(this);
    }
}
