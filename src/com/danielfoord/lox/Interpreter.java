package com.danielfoord.lox;

import com.danielfoord.lox.expressions.*;
import com.danielfoord.lox.functions.LoxCallable;
import com.danielfoord.lox.functions.LoxFunction;
import com.danielfoord.lox.functions.Return;
import com.danielfoord.lox.globals.Clock;
import com.danielfoord.lox.globals.ReadLine;
import com.danielfoord.lox.statements.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Interpreter implements ExprVisitor<Object>, StmtVisitor<Object> {

    public final Environment globals = new Environment();
    private final Map<Expr, Integer> locals = new HashMap<>();
    public Environment environment = globals;
    private boolean hitBreak = false;

    Interpreter() {
        globals.define("clock", new Clock());
        globals.define("readLine", new ReadLine());
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
        LoxFunction function = new LoxFunction(statement, environment, false);
        environment.define(statement.name.lexeme, function);
        return null;
    }

    @Override
    public Object visitReturnStmt(ReturnStmt statement) {
        Object value = null;
        if (statement.value != null) value = evaluate(statement.value);
        throw new Return(value);
    }

    @Override
    public Object visitClassStmt(ClassStmt statement) {
        Object superClass = null;
        if (statement.superClass != null) {
            superClass = evaluate(statement.superClass);
            if (!(superClass instanceof LoxClass)) {
                throw new RuntimeError(statement.superClass.name, "Superclass must be a class.");
            }
        }

        environment.define(statement.name.lexeme, null);

        Map<String, LoxFunction> methods = new HashMap<>();
        for (Stmt method : statement.methods) {
            var fnStmt = (FunctionStmt) method;
            LoxFunction function = new LoxFunction(fnStmt, environment, fnStmt.name.lexeme.equals("init"));
            methods.put(((FunctionStmt) method).name.lexeme, function);
        }

        LoxClass klass = new LoxClass(statement.name.lexeme, (LoxClass) superClass, methods);
        environment.assign(statement.name, klass);
        return null;
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
            case PLUS_PLUS:
                return left.toString() + right.toString();
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

        Integer distance = locals.get(expression);
        if (distance != null) {
            environment.assignAt(distance, expression.name, value);
        } else {
            globals.assign(expression.name, value);
        }

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
        for (Expr arg : expression.arguments) {
            arguments.add(evaluate(arg));
        }

        if (!(callee instanceof LoxCallable)) {
            throw new RuntimeError(expression.paren, "Can only call functions and classes.");
        }

        LoxCallable function = (LoxCallable) callee;

        if (arguments.size() != function.arity()) {
            throw new RuntimeError(expression.paren, "Expected " +
                    function.arity() + " arguments but got " +
                    arguments.size() + ".");
        }

        try {
            return function.call(this, arguments);
        } catch (Exception error) {
            throw new RuntimeError(expression.paren, error.getMessage());
        }
    }

    @Override
    public Object visitGetExpr(GetExpr expression) {
        Object object = evaluate(expression.object);
        if (object instanceof LoxInstance) {
            return ((LoxInstance) object).get(expression.name);
        }

        throw new RuntimeError(expression.name, "Only instances have properties.");
    }

    @Override
    public Object visitSetExpr(SetExpr expression) {
        Object object = evaluate(expression.object);
        if (object instanceof LoxInstance) {
            ((LoxInstance) object).set(expression.name, evaluate(expression.value));
            return null;
        }

        throw new RuntimeError(expression.name, "Only instances have properties.");
    }

    @Override
    public Object visitThisExpr(ThisExpr expression) {
        return lookUpVariable(expression.keyword, expression);
    }

    @Override
    public Object visitVariableExpr(VariableExpr expression) {
        return lookUpVariable(expression.name, expression);
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

    private Object lookUpVariable(Token name, Expr expr) {
        Integer distance = locals.get(expr);
        if (distance != null) {
            return environment.getAt(distance, name.lexeme);
        } else {
            return globals.get(name);
        }
    }

    public void resolve(Expr expr, int depth) {
        locals.put(expr, depth);
    }
    //#endregion
}