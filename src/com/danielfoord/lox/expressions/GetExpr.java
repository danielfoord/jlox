package com.danielfoord.lox.expressions;

import com.danielfoord.lox.Token;

public class GetExpr extends Expr {

    public final Expr object;
    public final Token name;

    public GetExpr(Expr object, Token name) {
        this.object = object;
        this.name = name;
    }

    @Override
    public <R> R accept(ExprVisitor<R> visitor) {
        return visitor.visitGetExpr(this);
    }
}
