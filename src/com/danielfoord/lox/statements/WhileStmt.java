package com.danielfoord.lox.statements;

import com.danielfoord.lox.expressions.Expr;

public class WhileStmt extends Stmt {
    public final Expr condition;
    public final Stmt statement;

    public WhileStmt(Expr condition, Stmt statement) {
        this.condition = condition;
        this.statement = statement;
    }

    @Override
    public <R> R accept(StmtVisitor<R> visitor) {
        return visitor.visitWhileStmt(this);
    }
}
