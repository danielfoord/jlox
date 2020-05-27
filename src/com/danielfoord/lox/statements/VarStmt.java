package com.danielfoord.lox.statements;

import com.danielfoord.lox.Token;
import com.danielfoord.lox.expressions.Expr;

public class VarStmt extends Stmt {

    public final Token name;
    public final Expr initializer;

    public VarStmt(Token name, Expr initializer) {
        this.name = name;
        this.initializer = initializer;
    }

    @Override
    public <R> R accept(StmtVisitor<R> visitor) {
        return visitor.visitVarStmt(this);
    }
}