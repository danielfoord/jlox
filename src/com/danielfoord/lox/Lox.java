package com.danielfoord.lox;

import com.danielfoord.lox.statements.Stmt;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

class Lox {
    static boolean hadError = false;
    static boolean hadRuntimeError = false;
    static Interpreter interpreter = new Interpreter();

    private static void showCommands() {
        System.out.println("Usage: JLox [command]\n");
        System.out.println("Commands:");
        System.out.println("repl - Runs a REPL in the terminal");
        System.out.println("run [file] - Runs a lox file");
        System.out.println("compile [source] [output] - Compiles a lox file to an executable");
        System.out.println("execute [executable] [output] - Executes an executable");
    }

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            showCommands();
            System.exit(64);
        }

        switch (args[0]) {
            case "repl" -> runPrompt();
            case "run" -> runFile(args[1]);
            case "compile" -> compile(args[1], args[2]);
            case "execute" -> execute(args[1]);
            default -> {
                System.err.println("Unknown command '" + args[0] + "'\n");
                showCommands();
                System.exit(64);
            }
        }
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
        if (hadError)
            System.exit(65);
        if (hadRuntimeError)
            System.exit(70);
    }

    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (; ; ) {
            System.out.print(">> ");
            run(reader.readLine());
            hadError = false;
        }
    }

    private static void compile(String sourcePath, String outPath) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(sourcePath));
        String source = new String(bytes, Charset.defaultCharset());
        List<Stmt> statements = parse(source);

        if (hadError)
            System.exit(65);

        FileOutputStream fileStream = new FileOutputStream(outPath);
        ObjectOutputStream objectStream = new ObjectOutputStream(fileStream);
        objectStream.writeObject(statements);
        objectStream.close();
        fileStream.close();
    }

    private static void execute(String exePath) {
        try {
            FileInputStream fileStream = new FileInputStream(exePath);
            ObjectInputStream objectStream = new ObjectInputStream(fileStream);
            Object object = objectStream.readObject();

            objectStream.close();
            fileStream.close();

            if (!(object instanceof ArrayList)) {
                System.exit(65);
            }

            List<Stmt> statements = (ArrayList<Stmt>)object;

            Resolver resolver = new Resolver(interpreter);
            resolver.resolve(statements);

            interpreter.interpret(statements);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static List<Stmt> parse(String source)
    {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();

        // Stop if there was a syntax error.
        if (hadError)
            return null;

        Resolver resolver = new Resolver(interpreter);
        resolver.resolve(statements);

        return statements;
    }

    private static void run(String source) {

        var statements = parse(source);

        if (statements == null)
            return;

        interpreter.interpret(statements);
    }

    static void error(int line, String message) {
        report(line, "", message);
    }

    private static void report(int line, String where, String message) {
        System.err.println("[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }

    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }

    static void runtimeError(RuntimeError error) {
        System.err.println("Runtime Error: " + error.getMessage() + "\n[line " + error.token.line + "]");
        hadRuntimeError = true;
    }
}