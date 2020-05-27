package com.danielfoord.lox.statements;

public interface StmtVisitor<R> {
    R visitExpressionStmt(ExpressionStmt statement);
    R visitPrintStmt(PrintStmt statement);
    R visitVarStmt(VarStmt statement);
    R visitBlockStmt(BlockStmt statement);
}