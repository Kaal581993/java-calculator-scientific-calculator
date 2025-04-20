package com.example.calculator;
import java.util.*;

public class ScientificCalculator {

    // Enumeration to define token types.
    enum TokenType {
        NUMBER, OPERATOR, FUNCTION, PARENTHESIS, COMMA
    }

    // Token class encapsulating a token’s string and type.
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

    // Operator precedence map.
    static Map<String, Integer> precedence = new HashMap<>();
    static {
        precedence.put("+", 2);
        precedence.put("-", 2);
        precedence.put("*", 3);
        precedence.put("/", 3);
        precedence.put("^", 4);
    }

    // Function map: function name → number of arguments.
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
     * Evaluates the mathematical expression provided as a string.
     *
     * @param expression the expression to evaluate (e.g., "5+8", "sin(30)+sqrt(16)")
     * @return the computed result as a double.
     */
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

    /**
     * Splits the expression string into tokens.
     */
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
                // Build a number (including decimals)
                StringBuilder num = new StringBuilder();
                while (i < expr.length() && (Character.isDigit(expr.charAt(i)) || expr.charAt(i) == '.')) {
                    num.append(expr.charAt(i++));
                }
                tokens.add(new Token(num.toString(), TokenType.NUMBER));
            } else if (Character.isLetter(c)) {
                // Build a function name.
                StringBuilder func = new StringBuilder();
                while (i < expr.length() && Character.isLetter(expr.charAt(i))) {
                    func.append(expr.charAt(i++));
                }
                String funcStr = func.toString();
                // Mark it as a function if recognized; otherwise, treat as function (or variable) as needed.
                if (functions.containsKey(funcStr)) {
                    tokens.add(new Token(funcStr, TokenType.FUNCTION));
                } else {
                    tokens.add(new Token(funcStr, TokenType.FUNCTION)); // or throw error if unknown
                }
            } else if (c == ',') {
                tokens.add(new Token(",", TokenType.COMMA));
                i++;
            } else if (c == '(' || c == ')') {
                tokens.add(new Token(String.valueOf(c), TokenType.PARENTHESIS));
                i++;
            } else {
                // Assume it is an operator: +, -, *, /, ^
                String op = String.valueOf(c);
                if (precedence.containsKey(op)) {
                    tokens.add(new Token(op, TokenType.OPERATOR));
                }
                i++;
            }
        }
        return tokens;
    }

    /**
     * Converts an infix token list to Reverse Polish Notation (RPN) using the Shunting-yard algorithm.
     */
    private static List<Token> infixToRPN(List<Token> tokens) {
        List<Token> output = new ArrayList<>();
        Stack<Token> stack = new Stack<>();
        for (Token token : tokens) {
            switch (token.type) {
                case NUMBER:
                    output.add(token);
                    break;
                case FUNCTION:
                    stack.push(token);
                    break;
                case COMMA:
                    // Pop operators until left parenthesis is encountered.
                    while (!stack.isEmpty() && !stack.peek().token.equals("(")) {
                        output.add(stack.pop());
                    }
                    if (stack.isEmpty()) {
                        throw new RuntimeException("Misplaced separator or mismatched parentheses");
                    }
                    break;
                case OPERATOR:
                    while (!stack.isEmpty() && stack.peek().type == TokenType.OPERATOR &&
                           ((isLeftAssociative(token.token) &&
                             precedence.get(token.token) <= precedence.get(stack.peek().token))
                           || (!isLeftAssociative(token.token) &&
                              precedence.get(token.token) < precedence.get(stack.peek().token)))) {
                        output.add(stack.pop());
                    }
                    stack.push(token);
                    break;
                case PARENTHESIS:
                    if (token.token.equals("(")) {
                        stack.push(token);
                    } else if (token.token.equals(")")) {
                        // Pop until "(" is encountered.
                        while (!stack.isEmpty() && !stack.peek().token.equals("(")) {
                            output.add(stack.pop());
                        }
                        if (stack.isEmpty()) {
                            throw new RuntimeException("Mismatched parentheses");
                        }
                        stack.pop(); // Remove "("

                        // If a function is at the top of the stack, pop it to the output.
                        if (!stack.isEmpty() && stack.peek().type == TokenType.FUNCTION) {
                            output.add(stack.pop());
                        }
                    }
                    break;
            }
        }
        while (!stack.isEmpty()) {
            Token t = stack.pop();
            if (t.token.equals("(") || t.token.equals(")")) {
                throw new RuntimeException("Mismatched parentheses");
            }
            output.add(t);
        }
        return output;
    }

    // Determines if an operator is left-associative (all except "^" are left-associative).
    private static boolean isLeftAssociative(String op) {
        return !op.equals("^");
    }

    /**
     * Evaluates the RPN list of tokens and returns the result.
     */
    private static double evaluateRPN(List<Token> rpn) {
        Stack<Double> stack = new Stack<>();
        for (Token token : rpn) {
            if (token.type == TokenType.NUMBER) {
                stack.push(Double.parseDouble(token.token));
            } else if (token.type == TokenType.OPERATOR) {
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
                    args.add(0, stack.pop()); // add in reverse order
                }
                double res = applyFunction(token.token, args);
                stack.push(res);
            }
        }
        if (stack.size() != 1)
            throw new RuntimeException("Invalid Expression");
        return stack.pop();
    }

    /**
     * Applies the given operator to two operands.
     */
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

    /**
     * Applies the given function to its argument(s).
     */
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

    /**
     * Computes the factorial of a number.
     */
    public static double factorial(int n) {
        if (n < 0)
            throw new IllegalArgumentException("Negative factorial not allowed");
        double result = 1;
        for (int i = 2; i <= n; i++) {
            result *= i;
        }
        return result;
    }

    // A simple main method for local testing.
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
            "5+8*2-3/1"
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
