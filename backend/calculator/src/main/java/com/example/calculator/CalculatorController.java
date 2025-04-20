package com.example.calculator;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/calc")
@CrossOrigin(origins = "*")
public class CalculatorController {

    @PostMapping
    public CalculationResult calculate(@RequestBody ExpressionRequest request) {
        double result = ScientificCalculator.evaluate(request.getExpression());
        return new CalculationResult(result);
    }

    public static class ExpressionRequest {
        private String expression;
        public String getExpression() { return expression; }
        public void setExpression(String expression) { this.expression = expression; }
    }

    public static class CalculationResult {
        private double result;
        public CalculationResult(double result) { this.result = result; }
        public double getResult() { return result; }
    }
}
