package com.example.calculator;
import java.util.*;

public class ScientificCalculator {

    // Enum to classify the different types of tokens in an expression
    enum TokenType {
        NUMBER, OPERATOR, FUNCTION, PARENTHESIS, COMMA, UNARY_OPERATOR, CONSTANT
    }

    // Token class used to represent parts of the parsed expression
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

    // Map of operator precedence for infix to postfix conversion
    static Map<String, Integer> precedence = new HashMap<>();

    // Map of operator associativity (true for left, false for right)
    static Map<String, Boolean> associativity = new HashMap<>();

    static {
        // Set precedence for supported operators
        precedence.put("+", 2);
        precedence.put("-", 2);
        precedence.put("*", 3);
        precedence.put("/", 3);
        precedence.put("^", 4);

        // Set associativity
        associativity.put("+", true);
        associativity.put("-", true);
        associativity.put("*", true);
        associativity.put("/", true);
        associativity.put("^", false);
    }

    // Map of supported functions and their argument count
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

    /**
     * Main method to evaluate a mathematical expression string.
     */
    public static double evaluate(String expression) {
        try {
            List<Token> tokens = tokenize(expression); // Step 1: Convert string to tokens
            List<Token> rpn = infixToRPN(tokens);      // Step 2: Convert tokens to postfix using Shunting Yard
            double result = evaluateRPN(rpn);          // Step 3: Evaluate the postfix expression
            return result;
        } catch (Exception e) {
            e.printStackTrace(); // Log error details
            return 0;
        }
    }

    // Tokenizer: Converts input string into a list of tokens
    private static List<Token> tokenize(String expr) {
        List<Token> tokens = new ArrayList<>();
        int i = 0;
        while (i < expr.length()) {
            char c = expr.charAt(i);
            if (Character.isWhitespace(c)) {
                i++; continue;
            }

            // Handle numbers and decimals
            if (Character.isDigit(c) || c == '.') {
                StringBuilder num = new StringBuilder();
                while (i < expr.length() && (Character.isDigit(expr.charAt(i)) || expr.charAt(i) == '.')) {
                    num.append(expr.charAt(i++));
                }
                tokens.add(new Token(num.toString(), TokenType.NUMBER));
            }
            // Handle functions and constants
            else if (Character.isLetter(c)) {
                StringBuilder func = new StringBuilder();
                while (i < expr.length() && Character.isLetter(expr.charAt(i))) {
                    func.append(expr.charAt(i++));
                }
                String funcStr = func.toString();
                if (functions.containsKey(funcStr)) {
                    tokens.add(new Token(funcStr, TokenType.FUNCTION));
                } else {
                    tokens.add(new Token(funcStr, TokenType.CONSTANT)); // pi, e, etc.
                }
            }
            // Handle comma separator for multi-argument functions
            else if (c == ',') {
                tokens.add(new Token(",", TokenType.COMMA));
                i++;
            }
            // Handle parentheses
            else if (c == '(' || c == ')') {
                tokens.add(new Token(String.valueOf(c), TokenType.PARENTHESIS));
                i++;
            }
            // Handle operators
            else {
                String op = String.valueOf(c);
                if (precedence.containsKey(op)) {
                    tokens.add(new Token(op, TokenType.OPERATOR));
                }
                i++;
            }
        }
        return tokens;
    }

    // Converts infix expression tokens to postfix (RPN) using Shunting Yard Algorithm
    private static List<Token> infixToRPN(List<Token> tokens) {
        List<Token> output = new ArrayList<>();
        Stack<Token> stack = new Stack<>();

        for (Token token : tokens) {
            switch (token.type) {
                case NUMBER:
                case CONSTANT:
                    output.add(token); // Directly add literals
                    break;
                case FUNCTION:
                    stack.push(token); // Push function to stack
                    break;
                case COMMA:
                    // Pop operators until left parenthesis is found
                    while (!stack.isEmpty() && !stack.peek().token.equals("(")) {
                        output.add(stack.pop());
                    }
                    if (stack.isEmpty()) {
                        throw new RuntimeException("Misplaced comma or mismatched parentheses");
                    }
                    break;
                case OPERATOR:
                case UNARY_OPERATOR:
                    // Respect operator precedence and associativity
                    while (!stack.isEmpty() && stack.peek().type == TokenType.OPERATOR &&
                        ((isLeftAssociative(token.token) && precedence.get(token.token) <= precedence.get(stack.peek().token))
                        || (!isLeftAssociative(token.token) && precedence.get(token.token) < precedence.get(stack.peek().token)))) {
                        output.add(stack.pop());
                    }
                    stack.push(token);
                    break;
                case PARENTHESIS:
                    if (token.token.equals("(")) {
                        stack.push(token); // Push left parenthesis
                    } else {
                        // Process until left parenthesis
                        while (!stack.isEmpty() && !stack.peek().token.equals("(")) {
                            output.add(stack.pop());
                        }
                        if (stack.isEmpty()) {
                            throw new RuntimeException("Mismatched parentheses");
                        }
                        stack.pop(); // Remove the "("
                        // If top of stack is function, pop it to output
                        if (!stack.isEmpty() && stack.peek().type == TokenType.FUNCTION) {
                            output.add(stack.pop());
                        }
                    }
                    break;
            }
        }

        // Drain remaining stack to output
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
        return associativity.getOrDefault(op, true);
    }

    // Evaluates the RPN expression using a stack
    private static double evaluateRPN(List<Token> rpn) {
        Stack<Double> stack = new Stack<>();

        for (Token token : rpn) {
            if (token.type == TokenType.NUMBER) {
                stack.push(Double.parseDouble(token.token));
            } else if (token.type == TokenType.CONSTANT) {
                stack.push(getConstantValue(token.token));
            } else if (token.type == TokenType.OPERATOR || token.type == TokenType.UNARY_OPERATOR) {
                if (stack.size() < 2) {
                    throw new RuntimeException("Insufficient operands for operator: " + token.token);
                }
                double b = stack.pop();
                double a = stack.pop();
                double res = applyOperator(token.token, a, b);
                stack.push(res);
            } else if (token.type == TokenType.FUNCTION) {
                int argCount = functions.getOrDefault(token.token, 1);
                if (stack.size() < argCount) {
                    throw new RuntimeException("Insufficient arguments for function: " + token.token);
                }
                List<Double> args = new ArrayList<>();
                for (int i = 0; i < argCount; i++) {
                    args.add(0, stack.pop()); // Reverse order
                }
                double res = applyFunction(token.token, args);
                stack.push(res);
            }
        }

        if (stack.size() != 1) {
            throw new RuntimeException("Invalid expression: multiple values remaining");
        }
        return stack.pop();
    }

    // Returns constant values for known symbols
    private static double getConstantValue(String constant) {
        switch (constant) {
            case "pi": return Math.PI;
            case "e":  return Math.E;
            default:
                throw new RuntimeException("Unknown constant: " + constant);
        }
    }

    // Applies basic binary operators
    private static double applyOperator(String op, double a, double b) {
        switch (op) {
            case "+": return a + b;
            case "-": return a - b;
            case "*": return a * b;
            case "/": return a / b;
            case "^": return Math.pow(a, b);
            default:
                throw new RuntimeException("Unsupported operator: " + op);
        }
    }

    // Applies scientific functions
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

    // Computes factorial (for non-negative integers)
    public static double factorial(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Negative factorial is not defined");
        }
        double result = 1;
        for (int i = 2; i <= n; i++) {
            result *= i;
        }
        return result;
    }

    // Test cases for verification
    public static void main(String[] args) {
        String[] expressions = {
            "5+8", "sin(30)", "cos(60)", "sqrt(16)", "log(100)",
            "ln(2.71828)", "exp(1)", "fact(5)", "pow(2,3)",
            "5+8*2", "5+8*2-3/1", "pi + e"
        };

        for (String expr : expressions) {
            try {
                System.out.println(expr + " = " + evaluate(expr));
            } catch (Exception e) {
                System.out.println("Error evaluating: " + expr);
                e.printStackTrace();
            }
        }
    }
}
