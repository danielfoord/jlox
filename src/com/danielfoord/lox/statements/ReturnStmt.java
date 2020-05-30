package com.danielfoord.lox.statements;

import com.danielfoord.lox.Token;
import com.danielfoord.lox.expressions.Expr;

public class ReturnStmt extends Stmt {
    public final Token keyword;
    public final Expr value;

    public ReturnStmt(Token keyword, Expr value) {
        this.keyword = keyword;
        this.value = value;
    }

    @Override
    public <R> R accept(StmtVisitor<R> visitor) {
        return visitor.visitReturnStmt(this);
    }
}
