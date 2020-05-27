package com.danielfoord.lox.statements;

import com.danielfoord.lox.expressions.Expr;

public class ExpressionStmt extends Stmt {

    public final Expr expression;

    public ExpressionStmt(Expr expression) {
        this.expression = expression;
    }

    @Override
    public <R> R accept(StmtVisitor<R> visitor) {
        return visitor.visitExpressionStmt(this);
    }

}