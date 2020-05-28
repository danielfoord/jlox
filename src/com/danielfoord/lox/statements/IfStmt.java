package com.danielfoord.lox.statements;

import com.danielfoord.lox.expressions.Expr;

public class IfStmt extends Stmt {
    public final Expr condition;
    public final Stmt ifStatement;
    public final Stmt elseStatement;

    public IfStmt(Expr condition, Stmt ifStatement, Stmt elseStatement) {
        this.condition = condition;
        this.ifStatement = ifStatement;
        this.elseStatement = elseStatement;
    }

    @Override
    public <R> R accept(StmtVisitor<R> visitor) {
        return visitor.visitIfStmt(this);
    }
}
