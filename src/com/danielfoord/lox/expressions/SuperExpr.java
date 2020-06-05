package com.danielfoord.lox.expressions;

import com.danielfoord.lox.Token;

public class SuperExpr extends Expr {

    public final Token keyword;
    public final Token method;

    public SuperExpr(Token keyword, Token method) {
        this.keyword = keyword;
        this.method = method;
    }

    @Override
    public <R> R accept(ExprVisitor<R> visitor) {
        return visitor.visitSuperExpr(this);
    }
}
