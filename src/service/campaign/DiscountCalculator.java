package service.campaign;

public class DiscountCalculator {

    /**
     * Applies percentage discount to price
     * Example: price=100, discountRate=20 → 80
     */
    public double applyDiscount(double price, double discountRate) {

        if (price < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }

        if (discountRate < 0) {
            discountRate = 0;
        }

        if (discountRate > 100) {
            discountRate = 100;
        }

        return price * (1 - (discountRate / 100.0));
    }
}