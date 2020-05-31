package com.danielfoord.lox.expressions;

import com.danielfoord.lox.Token;

public class ThisExpr extends Expr {

    public final Token keyword;

    public ThisExpr(Token keyword) {
        this.keyword = keyword;
    }

    @Override
    public <R> R accept(ExprVisitor<R> visitor) {
        return visitor.visitThisExpr(this);
    }
}
