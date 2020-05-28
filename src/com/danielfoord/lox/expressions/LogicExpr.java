package com.danielfoord.lox.expressions;

import com.danielfoord.lox.Token;

public class LogicExpr extends Expr {
    public final Expr left;
    public final Token operator;
    public final Expr right;

    public LogicExpr(Expr left, Token operator, Expr right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public <R> R accept(ExprVisitor<R> visitor) {
        return visitor.visitLogicExpr(this);
    }
}
