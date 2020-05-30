package com.danielfoord.lox;

import com.danielfoord.lox.expressions.*;
import com.danielfoord.lox.statements.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Resolver implements StmtVisitor<Void>, ExprVisitor<Void> {

    private final Interpreter interpreter;
    private final Stack<Map<String, ScopeVariable>> scopes = new Stack<>();
    private FunctionType currentFunction = FunctionType.NONE;

    public Resolver(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    //#region Statements
    @Override
    public Void visitExpressionStmt(ExpressionStmt statement) {
        resolve(statement.expression);
        return null;
    }

    @Override
    public Void visitPrintStmt(PrintStmt statement) {
        resolve(statement.expression);
        return null;
    }

    @Override
    public Void visitVarStmt(VarStmt statement) {
        declare(statement.name);
        if (statement.initializer != null) {
            resolve(statement.initializer);
        }
        define(statement.name);
        return null;
    }

    @Override
    public Void visitBlockStmt(BlockStmt statement) {
        beginScope();
        resolve(statement.statements);
        scopes.peek()
                .entrySet()
                .stream()
                .filter(e -> e.getValue().state == VariableState.DEFINED).forEach(stringScopeVariableEntry ->
                Lox.error(stringScopeVariableEntry.getValue().declarationToken, "Unused local variable")
        );
        endScope();
        return null;
    }

    @Override
    public Void visitIfStmt(IfStmt statement) {
        resolve(statement.condition);
        resolve(statement.ifStatement);
        if (statement.elseStatement != null) resolve(statement.elseStatement);
        return null;
    }

    @Override
    public Void visitWhileStmt(WhileStmt statement) {
        resolve(statement.condition);
        resolve(statement.statement);
        return null;
    }

    @Override
    public Void visitBreakStmt(BreakStmt statement) {
        return null;
    }

    @Override
    public Void visitFunctionStmt(FunctionStmt statement) {
        declare(statement.name);
        define(statement.name);
        resolveFunction(statement, FunctionType.FUNCTION);
        return null;
    }

    @Override
    public Void visitReturnStmt(ReturnStmt statement) {
        if (currentFunction == FunctionType.NONE) {
            Lox.error(statement.keyword, "Cannot return from top-level code.");
        }
        if (statement.value != null) {
            resolve(statement.value);
        }
        return null;
    }
    //#endregion

    //#region Expressions
    @Override
    public Void visitBinaryExpr(BinaryExpr expression) {
        resolve(expression.left);
        resolve(expression.right);
        return null;
    }

    @Override
    public Void visitLiteralExpr(LiteralExpr expression) {
        return null;
    }

    @Override
    public Void visitGroupingExpr(GroupingExpr expression) {
        resolve(expression.expression);
        return null;
    }

    @Override
    public Void visitUnaryExpr(UnaryExpr expression) {
        resolve(expression.right);
        return null;
    }

    @Override
    public Void visitVariableExpr(VariableExpr expression) {
        var scopeVariable = scopes.peek().get(expression.name.lexeme);
        if (scopeVariable != null) {
            if (!scopes.isEmpty() && scopeVariable.state == VariableState.DECLARED) {
                Lox.error(expression.name, "Cannot read local variable in its own initializer.");
            }
        }

        resolveLocal(expression, expression.name);
        return null;
    }

    @Override
    public Void visitAssignExpr(AssignExpr expression) {
        resolve(expression.value);
        resolveLocal(expression, expression.name);
        return null;
    }

    @Override
    public Void visitLogicExpr(LogicExpr expression) {
        resolve(expression.left);
        resolve(expression.right);
        return null;
    }

    @Override
    public Void visitCallExpr(CallExpr expression) {
        resolve(expression.callee);
        for (Expr arg : expression.arguments) {
            resolve(arg);
        }
        return null;
    }
    //#endregion

    //#region Util
    public void resolve(List<Stmt> statements) {
        for (Stmt statement : statements) {
            resolve(statement);
        }
    }

    private void resolve(Stmt stmt) {
        stmt.accept(this);
    }

    private void resolve(Expr expr) {
        expr.accept(this);
    }

    private void beginScope() {
        scopes.push(new HashMap<>());
    }

    private void endScope() {
        scopes.pop();
    }

    private void declare(Token name) {
        if (scopes.empty()) return;
        Map<String, ScopeVariable> scope = scopes.peek();
        if (scope.containsKey(name.lexeme)) {
            Lox.error(name, "Variable with this name already declared in this scope.");
        }
        scope.put(name.lexeme, new ScopeVariable(name, VariableState.DECLARED));
    }

    private void define(Token name) {
        if (scopes.isEmpty()) return;
        scopes.peek().put(name.lexeme, new ScopeVariable(name, VariableState.DEFINED));
    }

    private void resolveLocal(Expr expression, Token name) {
        for (var i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(name.lexeme)) {
                scopes.get(i).put(name.lexeme, new ScopeVariable(name, VariableState.ACCESSED));
                interpreter.resolve(expression, scopes.size() - 1 - i);
                return;
            }
        }
    }

    private void resolveFunction(FunctionStmt function, FunctionType fnType) {
        FunctionType enclosingFunction = currentFunction;
        currentFunction = fnType;

        beginScope();
        for (Token param : function.parameters) {
            declare(param);
            define(param);
        }
        resolve(function.body);
        endScope();

        currentFunction = enclosingFunction;
    }
    //#endregion

    private enum FunctionType {
        NONE,
        FUNCTION
    }

    private enum VariableState {
        DECLARED,
        DEFINED,
        ACCESSED
    }

    static final class ScopeVariable {
        public final Token declarationToken;
        public final VariableState state;

        ScopeVariable(Token declarationToken, VariableState state) {
            this.declarationToken = declarationToken;
            this.state = state;
        }
    }
}

