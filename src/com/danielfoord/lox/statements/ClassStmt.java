package com.danielfoord.lox.statements;

import com.danielfoord.lox.Token;

import java.util.List;

public class ClassStmt extends Stmt {

    public final Token name;
    public final List<Stmt> methods;

    public ClassStmt(Token name, List<Stmt> methods) {
        this.name = name;
        this.methods = methods;
    }

    @Override
    public <R> R accept(StmtVisitor<R> visitor) {
        return visitor.visitClassStmt(this);
    }
}
