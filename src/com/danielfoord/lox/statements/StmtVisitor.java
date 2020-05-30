package com.danielfoord.lox.statements;

public interface StmtVisitor<R> {
    R visitExpressionStmt(ExpressionStmt statement);

    R visitPrintStmt(PrintStmt statement);

    R visitVarStmt(VarStmt statement);

    R visitBlockStmt(BlockStmt statement);

    R visitIfStmt(IfStmt statement);

    R visitWhileStmt(WhileStmt statement);

    R visitBreakStmt(BreakStmt statement);

    R visitFunctionStmt(FunctionStmt statement);

    R visitReturnStmt(ReturnStmt statement);

    R visitClassStmt(ClassStmt statement);
}