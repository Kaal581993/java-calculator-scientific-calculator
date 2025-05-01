package com.example.calculator;
import java.util.*;

public class ScientificCalculator {

    enum TokenType {
        NUMBER, OPERATOR, FUNCTION, PARENTHESIS, COMMA, UNARY_OPERATOR, CONSTANT
    }

    static class Token {
        String token;
        TokenType type;

        Token(String token, TokenType type) {
            this.token = token;
            this.type = type;
        }

        @Override
        public String toString() {
            return token;
        }
    }

    static Map<String, Integer> precedence = new HashMap<>();
    static {
        precedence.put("+", 2);
        precedence.put("-", 2);
        precedence.put("*", 3);
        precedence.put("/", 3);
        precedence.put("^", 4);
    }

    static Map<String, Integer> functions = new HashMap<>();
    static {
        functions.put("sin", 1);
        functions.put("cos", 1);
        functions.put("tan", 1);
        functions.put("sqrt", 1);
        functions.put("log", 1);
        functions.put("ln", 1);
        functions.put("exp", 1);
        functions.put("fact", 1);
        functions.put("pow", 2);
    }

    public static double evaluate(String expression) {
        try {
            List<Token> tokens = tokenize(expression);
            List<Token> rpn = infixToRPN(tokens);
            double result = evaluateRPN(rpn);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private static List<Token> tokenize(String expr) {
        List<Token> tokens = new ArrayList<>();
        int i = 0;
        while (i < expr.length()) {
            char c = expr.charAt(i);
            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }
            if (Character.isDigit(c) || c == '.') {
                StringBuilder num = new StringBuilder();
                while (i < expr.length() && (Character.isDigit(expr.charAt(i)) || expr.charAt(i) == '.')) {
                    num.append(expr.charAt(i++));
                }
                tokens.add(new Token(num.toString(), TokenType.NUMBER));
            } else if (Character.isLetter(c)) {
                StringBuilder func = new StringBuilder();
                while (i < expr.length() && Character.isLetter(expr.charAt(i))) {
                    func.append(expr.charAt(i++));
                }
                String funcStr = func.toString();
                if (functions.containsKey(funcStr)) {
                    tokens.add(new Token(funcStr, TokenType.FUNCTION));
                } else {
                    tokens.add(new Token(funcStr, TokenType.CONSTANT));
                }
            } else if (c == ',') {
                tokens.add(new Token(",", TokenType.COMMA));
                i++;
            } else if (c == '(' || c == ')') {
                tokens.add(new Token(String.valueOf(c), TokenType.PARENTHESIS));
                i++;
            } else {
                String op = String.valueOf(c);
                if (precedence.containsKey(op)) {
                    tokens.add(new Token(op, TokenType.OPERATOR));
                }
                i++;
            }
        }
        return tokens;
    }

    private static List<Token> infixToRPN(List<Token> tokens) {
        List<Token> output = new ArrayList<>(); // Output list for RPN
        Stack<Token> stack = new Stack<>(); // Stack to hold operators and functions
        for (Token token : tokens) {
            switch (token.type) {
                case NUMBER:
                case CONSTANT:
                    output.add(token); // Numbers and constants go directly to output
                    break;
                case FUNCTION:
                    stack.push(token); // Push functions onto the stack
                    break;
                case COMMA:
                    // Pop operators until left parenthesis is found
                    while (!stack.isEmpty() && !stack.peek().token.equals("(")) {
                        output.add(stack.pop());
                    }
                    if (stack.isEmpty()) {
                        throw new RuntimeException("Misplaced separator or mismatched parentheses");
                    }
                    break;
                case OPERATOR:
                case UNARY_OPERATOR:
                    // Pop operators from stack to output based on precedence and associativity
                    while (!stack.isEmpty() && stack.peek().type == TokenType.OPERATOR &&
                           ((isLeftAssociative(token.token) &&
                             precedence.get(token.token) <= precedence.get(stack.peek().token))
                           || (!isLeftAssociative(token.token) &&
                              precedence.get(token.token) < precedence.get(stack.peek().token)))) {
                        output.add(stack.pop());
                    }
                    stack.push(token); // Push the current operator
                    break;
                case PARENTHESIS:
                    if (token.token.equals("(")) {
                        stack.push(token); // Push left parenthesis
                    } else if (token.token.equals(")")) {
                        // Pop until matching "(" is found
                        while (!stack.isEmpty() && !stack.peek().token.equals("(")) {
                            output.add(stack.pop());
                        }
                        if (stack.isEmpty()) {
                            throw new RuntimeException("Mismatched parentheses");
                        }
                        stack.pop(); // Pop the left parenthesis

                        // If a function is on top, pop it to output
                        if (!stack.isEmpty() && stack.peek().type == TokenType.FUNCTION) {
                            output.add(stack.pop());
                        }
                    }
                    break;
            }
        }
        // Pop remaining items from stack to output
        while (!stack.isEmpty()) {
            Token t = stack.pop();
            if (t.token.equals("(") || t.token.equals(")")) {
                throw new RuntimeException("Mismatched parentheses");
            }
            output.add(t);
        }
        return output;
    }

    private static boolean isLeftAssociative(String op) {
        return !op.equals("^");
    }

    private static double evaluateRPN(List<Token> rpn) {
        Stack<Double> stack = new Stack<>();
        for (Token token : rpn) {
            if (token.type == TokenType.NUMBER) {
                stack.push(Double.parseDouble(token.token));
            } else if (token.type == TokenType.CONSTANT) {
                stack.push(getConstantValue(token.token));
            } else if (token.type == TokenType.OPERATOR || token.type == TokenType.UNARY_OPERATOR) {
                if (stack.size() < 2)
                    throw new RuntimeException("Insufficient values for operator " + token.token);
                double b = stack.pop();
                double a = stack.pop();
                double res = applyOperator(token.token, a, b);
                stack.push(res);
            } else if (token.type == TokenType.FUNCTION) {
                int argCount = functions.getOrDefault(token.token, 1);
                if (stack.size() < argCount)
                    throw new RuntimeException("Insufficient values for function " + token.token);
                List<Double> args = new ArrayList<>();
                for (int i = 0; i < argCount; i++) {
                    args.add(0, stack.pop());
                }
                double res = applyFunction(token.token, args);
                stack.push(res);
            }
        }
        if (stack.size() != 1)
            throw new RuntimeException("Invalid Expression");
        return stack.pop();
    }

    private static double getConstantValue(String constant) {
        switch (constant) {
            case "pi": return Math.PI;
            case "e": return Math.E;
            default:
                throw new RuntimeException("Unknown constant: " + constant);
        }
    }

    private static double applyOperator(String op, double a, double b) {
        switch (op) {
            case "+":  return a + b;
            case "-":  return a - b;
            case "*":  return a * b;
            case "/":  return a / b;
            case "^":  return Math.pow(a, b);
            default:
                throw new RuntimeException("Unknown operator: " + op);
        }
    }

    private static double applyFunction(String func, List<Double> args) {
        switch (func) {
            case "sin":  return Math.sin(Math.toRadians(args.get(0)));
            case "cos":  return Math.cos(Math.toRadians(args.get(0)));
            case "tan":  return Math.tan(Math.toRadians(args.get(0)));
            case "sqrt": return Math.sqrt(args.get(0));
            case "log":  return Math.log10(args.get(0));
            case "ln":   return Math.log(args.get(0));
            case "exp":  return Math.exp(args.get(0));
            case "fact": return factorial((int) Math.round(args.get(0)));
            case "pow":  return Math.pow(args.get(0), args.get(1));
            default:
                throw new RuntimeException("Unknown function: " + func);
        }
    }

    public static double factorial(int n) {
        if (n < 0)
            throw new IllegalArgumentException("Negative factorial not allowed");
        double result = 1;
        for (int i = 2; i <= n; i++) {
            result *= i;
        }
        return result;
    }

    public static void main(String[] args) {
        String[] expressions = {
            "5+8",
            "sin(30)",
            "cos(60)",
            "sqrt(16)",
            "log(100)",
            "ln(2.71828)",
            "exp(1)",
            "fact(5)",
            "pow(2,3)",
            "5+8*2",
            "5+8*2-3/1",
            "pi + e"
        };

        for (String expr : expressions) {
            try {
                System.out.println(expr + " = " + evaluate(expr));
            } catch (Exception e) {
                System.out.println("Error in expression: " + expr);
                e.printStackTrace();
            }
        }
    }
}
