package com.danielfoord.lox.statements;

import com.danielfoord.lox.Token;
import com.danielfoord.lox.expressions.VariableExpr;

import java.util.List;

public class ClassStmt extends Stmt {

    public final Token name;
    public final VariableExpr superClass;
    public final List<Stmt> methods;

    public ClassStmt(Token name, VariableExpr superClass, List<Stmt> methods) {
        this.name = name;
        this.superClass = superClass;
        this.methods = methods;
    }

    @Override
    public <R> R accept(StmtVisitor<R> visitor) {
        return visitor.visitClassStmt(this);
    }
}
