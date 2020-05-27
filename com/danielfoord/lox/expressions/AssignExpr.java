package com.danielfoord.lox.expressions;

import com.danielfoord.lox.Token;

public class AssignExpr extends Expr {

    public final Token name;
    public final Expr value;

    public AssignExpr(Token name, Expr value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public <R> R accept(ExprVisitor<R> visitor) {
        return visitor.visitAssignExpr(this);
    }
}