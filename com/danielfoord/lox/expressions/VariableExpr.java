package com.danielfoord.lox.expressions;

import com.danielfoord.lox.Token;

public class VariableExpr extends Expr {
    public final Token name;

    public VariableExpr(final Token name) {
        this.name = name;
    }

    @Override
    public <R> R accept(ExprVisitor<R> visitor) {
        return visitor.visitVariableExpr(this);
    }
}