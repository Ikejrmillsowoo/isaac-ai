package com.isaacai.tools;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ToolCalculatorTest {
 @Test void exactDecimal() { assertEquals("0.3",ToolCalculator.calculate("0.1","+","0.2")); }
 @Test void multiplication() { assertEquals("42",ToolCalculator.calculate("6","*","7")); }
 @Test void rejectsUnsupported() { assertThrows(IllegalArgumentException.class,()->ToolCalculator.calculate("2","^","3")); }
 @Test void rejectsDivisionByZero() { assertThrows(ArithmeticException.class,()->ToolCalculator.calculate("2","/","0")); }
}
