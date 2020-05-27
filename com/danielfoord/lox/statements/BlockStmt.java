package com.danielfoord.lox.statements;

import java.util.List;

public class BlockStmt extends Stmt {

    public final List<Stmt> statements;
    
    public BlockStmt(List<Stmt> statements) {
        this.statements = statements;
    }

    @Override
    public <R> R accept(StmtVisitor<R> visitor) {
        return visitor.visitBlockStmt(this);
    }
}