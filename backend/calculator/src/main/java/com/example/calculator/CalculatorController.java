// Declare the package for the class
package com.example.calculator;

// Import necessary Spring annotations and classes for REST APIs
import org.springframework.web.bind.annotation.*;

@RestController  // Marks this class as a REST controller (methods return JSON/XML responses)
@RequestMapping("/api/calc")  // Base path for all endpoints in this controller
@CrossOrigin(origins = "*")  // Allow requests from any origin (CORS configuration)
public class CalculatorController {

    // Handle HTTP POST requests to /api/calc
    @PostMapping
    public CalculationResult calculate(@RequestBody ExpressionRequest request) {
        // Evaluate the expression received from the request body using ScientificCalculator
        double result = ScientificCalculator.evaluate(request.getExpression());
        // Return the result wrapped in a CalculationResult object
        return new CalculationResult(result);
    }

    // Inner class to map incoming JSON request body
    public static class ExpressionRequest {
        private String expression;  // Field to hold the mathematical expression string

        // Getter for expression
        public String getExpression() { return expression; }

        // Setter for expression
        public void setExpression(String expression) { this.expression = expression; }
    }

    // Inner class to structure the JSON response
    public static class CalculationResult {
        private double result;  // Field to hold the computed result

        // Constructor to set the result
        public CalculationResult(double result) { this.result = result; }

        // Getter to retrieve the result value
        public double getResult() { return result; }
    }
}
