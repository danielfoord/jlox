package com.danielfoord.lox.expressions;

import com.danielfoord.lox.Token;

public class SetExpr extends Expr {

    public final Expr object;
    public final Token name;
    public final Expr value;

    public SetExpr(Expr object, Token name, Expr value) {
        this.object = object;
        this.name = name;
        this.value = value;
    }

    @Override
    public <R> R accept(ExprVisitor<R> visitor) {
        return visitor.visitSetExpr(this);
    }
}
