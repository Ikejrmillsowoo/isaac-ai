package com.isaacai.tools;

import java.math.BigDecimal;
import java.math.MathContext;

public final class ToolCalculator {
    private ToolCalculator() {}
    public static String calculate(String left, String operator, String right) {
        if (left == null || right == null || left.length()>100 || right.length()>100 ||
            !left.matches("[+-]?[0-9]+(\\.[0-9]+)?") || !right.matches("[+-]?[0-9]+(\\.[0-9]+)?"))
            throw new IllegalArgumentException("Invalid decimal");
        BigDecimal a = new BigDecimal(left), b = new BigDecimal(right);
        BigDecimal result = switch(operator) {
            case "+" -> a.add(b);
            case "-" -> a.subtract(b);
            case "*" -> a.multiply(b);
            case "/" -> a.divide(b, MathContext.DECIMAL128);
            default -> throw new IllegalArgumentException("Unsupported operator");
        };
        return result.stripTrailingZeros().toPlainString();
    }
}
