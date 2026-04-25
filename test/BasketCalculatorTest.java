package test;

import model.Basket;
import org.junit.jupiter.api.Test;
import service.pricing.BasketCalculator;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BasketCalculatorTest {
    @Test
    public void testCalculateBasketTotalStub() {
        BasketCalculator calculator = new BasketCalculator();
        Basket dummyBasket = new Basket(1);
        assertEquals(145.50, calculator.calculateBasketTotal(dummyBasket));
    }
}