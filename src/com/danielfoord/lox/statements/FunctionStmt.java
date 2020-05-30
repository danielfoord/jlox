package com.danielfoord.lox.statements;

import com.danielfoord.lox.Token;

import java.util.List;

public class FunctionStmt extends Stmt {
    public final Token name;
    public final List<Token> parameters;
    public final List<Stmt> body;

    public FunctionStmt(Token name, List<Token> parameters, List<Stmt> body) {
        this.name = name;
        this.parameters = parameters;
        this.body = body;
    }

    @Override
    public <R> R accept(StmtVisitor<R> visitor) {
        return visitor.visitFunctionStmt(this);
    }
}
