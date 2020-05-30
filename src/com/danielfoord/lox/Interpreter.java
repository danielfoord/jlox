package com.danielfoord.lox;

import com.danielfoord.lox.expressions.*;
import com.danielfoord.lox.functions.LoxCallable;
import com.danielfoord.lox.functions.LoxFunction;
import com.danielfoord.lox.functions.Return;
import com.danielfoord.lox.statements.*;

import java.util.ArrayList;
import java.util.List;

public class Interpreter implements ExprVisitor<Object>, StmtVisitor<Object> {
    public final Environment globals = new Environment();
    public Environment environment = globals;
    private boolean hitBreak = false;

    Interpreter() {
        globals.define("clock", new LoxCallable() {
            @Override
            public int arity() { return 0; }

            @Override
            public Object call(Interpreter interpreter,
                               List<Object> arguments) {
                return (double)System.currentTimeMillis() / 1000.0;
            }

            @Override
            public String toString() { return "<native fn>"; }
        });
    }

    //#region Statements
    @Override
    public Void visitExpressionStmt(ExpressionStmt statement) {
        evaluate(statement.expression);
        return null;
    }

    @Override
    public Object visitPrintStmt(PrintStmt statement) {
        Object value = evaluate(statement.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Object visitVarStmt(VarStmt statement) {
        Object value = null;
        if (statement.initializer != null) {
            value = evaluate(statement.initializer);
        }

        environment.define(statement.name.lexeme, value);
        return null;
    }

    @Override
    public Object visitBlockStmt(BlockStmt statement) {
        executeBlock(statement.statements, new Environment(this.environment));
        return null;
    }

    @Override
    public Object visitIfStmt(IfStmt statement) {
        if (isTruthy(evaluate(statement.condition))) {
            execute(statement.ifStatement);
        } else if (statement.elseStatement != null) {
            execute(statement.elseStatement);
        }
        return null;
    }

    @Override
    public Object visitWhileStmt(WhileStmt statement) {
        while (!hitBreak && isTruthy(evaluate(statement.condition))) {
            execute(statement.statement);
        }
        hitBreak = false;
        return null;
    }

    @Override
    public Object visitBreakStmt(BreakStmt statement) {
        hitBreak = true;
        return null;
    }

    @Override
    public Object visitFunctionStmt(FunctionStmt statement) {
        LoxFunction function = new LoxFunction(statement);
        environment.define(statement.name.lexeme, function);
        return null;
    }

    @Override
    public Object visitReturnStmt(ReturnStmt statement) {
        Object value = null;
        if (statement.value != null) value = evaluate(statement.value);
        throw new Return(value);
    }

    public void executeBlock(List<Stmt> statements, Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;
            for (Stmt stmt : statements) {
                execute(stmt);
                if (hitBreak) {
                    break;
                }
            }
        } finally {
            this.environment = previous;
        }
    }
    //#endregion

    //#region Expressions
    @Override
    public Object visitBinaryExpr(BinaryExpr expression) {
        Object left = evaluate(expression.left);
        Object right = evaluate(expression.right);

        switch (expression.operator.type) {
            case MINUS:
                assertNumberOperand(expression.operator, right);
                return (double) left - (double) right;
            case SLASH:
                assertOperandTypes(expression.operator, left, right);
                return (double) left / (double) right;
            case STAR:
                assertOperandTypes(expression.operator, left, right);
                return (double) left * (double) right;
            case PLUS:
                assertOperandTypes(expression.operator, left, right);
                return (double) left + (double) right;
            case GREATER:
                assertOperandTypes(expression.operator, left, right);
                return (double) left > (double) right;
            case GREATER_EQUAL:
                assertOperandTypes(expression.operator, left, right);
                return (double) left >= (double) right;
            case LESS:
                assertOperandTypes(expression.operator, left, right);
                return (double) left < (double) right;
            case LESS_EQUAL:
                assertOperandTypes(expression.operator, left, right);
                return (double) left <= (double) right;
            case EQUAL_EQUAL:
                assertOperandTypesMatch(expression.operator, left, right);
                return isEqual(left, right);
            case BANG_EQUAL:
                return !isEqual(left, right);
            default:
                break;
        }

        return null;
    }

    @Override
    public Object visitLiteralExpr(LiteralExpr expression) {
        return expression.value;
    }

    @Override
    public Object visitGroupingExpr(GroupingExpr expression) {
        return evaluate(expression.expression);
    }

    @Override
    public Object visitUnaryExpr(UnaryExpr expression) {
        Object right = evaluate(expression.right);

        if (expression.operator.type == TokenType.MINUS) {
            assertNumberOperand(expression.operator, right);
            return -(double) right;
        } else if (expression.operator.type == TokenType.BANG) {
            return !isTruthy(right);
        }

        return null;
    }

    @Override
    public Object visitAssignExpr(AssignExpr expression) {
        Object value = evaluate(expression.value);
        environment.assign(expression.name, value);
        return value;
    }

    @Override
    public Object visitLogicExpr(LogicExpr expression) {
        Object left = evaluate(expression.left);

        if (expression.operator.type == TokenType.OR) {
            if (isTruthy(left)) return left;
        } else {
            if (!isTruthy(left)) return left;
        }

        return evaluate(expression.right);
    }

    @Override
    public Object visitCallExpr(CallExpr expression) {
        Object callee = evaluate(expression.callee);

        List<Object> arguments = new ArrayList<>();
        for(Expr arg : expression.arguments) {
            arguments.add(evaluate(arg));
        }

        if (!(callee instanceof LoxCallable)) {
            throw new RuntimeError(expression.paren, "Can only call functions and classes.");
        }

        LoxCallable function = (LoxCallable)callee;

        if (arguments.size() != function.arity()) {
            throw new RuntimeError(expression.paren, "Expected " +
                    function.arity() + " arguments but got " +
                    arguments.size() + ".");
        }

        return function.call(this, arguments);
    }

    @Override
    public Object visitVariableExpr(VariableExpr expression) {
        return environment.get(expression.name);
    }
    //#endregion

    //#region Util
    public void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    private boolean isTruthy(Object object) {
        if (object == null)
            return false;
        else if (object instanceof Boolean)
            return (boolean) object;
        else if (object instanceof Double)
            return (double) object > 0;
        else if (object instanceof String)
            return !object.equals("");
        return true;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null)
            return true;
        if (a == null)
            return false;
        return a.equals(b);
    }

    private void assertNumberOperand(Token token, Object operand) throws RuntimeError {
        if (operand instanceof Double)
            return;
        throw new RuntimeError(token, "Expected number operand");
    }

    private void assertOperandTypes(Token token, Object left, Object right) {
        if (left instanceof Double && right instanceof Double)
            return;
        throw new RuntimeError(token, "Expected number operand");
    }

    private void assertOperandTypesMatch(Token token, Object a, Object b) {
        if (a == null || b == null)
            return;
        if (a.getClass() == b.getClass())
            return;

        String expectedOperandType = null;
        if (a instanceof String)
            expectedOperandType = "String";
        if (a instanceof Double)
            expectedOperandType = "Number";
        if (a instanceof Boolean)
            expectedOperandType = "Boolean";

        throw new RuntimeError(token, "Expected operand type " + expectedOperandType);
    }

    private String stringify(Object object) {
        if (object == null)
            return "nil";

        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }
        return object.toString();
    }
    //#endregion
}