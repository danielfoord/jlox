package com.danielfoord.lox.expressions;

public interface ExprVisitor<R> {
    R visitBinaryExpr(BinaryExpr expression);

    R visitLiteralExpr(LiteralExpr expression);

    R visitGroupingExpr(GroupingExpr expression);

    R visitUnaryExpr(UnaryExpr expression);

    R visitVariableExpr(VariableExpr expression);

    R visitAssignExpr(AssignExpr expression);

    R visitLogicExpr(LogicExpr expression);

    R visitCallExpr(CallExpr expression);

    R visitGetExpr(GetExpr expression);

    R visitSetExpr(SetExpr expression);

    R visitThisExpr(ThisExpr expression);
}