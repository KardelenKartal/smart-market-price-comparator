package test;

import org.junit.jupiter.api.Test;
import service.campaign.DiscountCalculator;

import static org.junit.jupiter.api.Assertions.*;

public class DiscountCalculatorTest {

    @Test
    void shouldApplyDiscountCorrectly() {
        DiscountCalculator calculator = new DiscountCalculator();

        double result = calculator.applyDiscount(100.0, 20.0);

        assertEquals(80.0, result, 0.001);
    }

    @Test
    void shouldHandleZeroDiscount() {
        DiscountCalculator calculator = new DiscountCalculator();

        double result = calculator.applyDiscount(100.0, 0.0);

        assertEquals(100.0, result, 0.001);
    }
}