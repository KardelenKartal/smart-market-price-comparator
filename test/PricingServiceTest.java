package test;

import org.junit.jupiter.api.Test;
import service.pricing.PricingService;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PricingServiceTest {
    @Test
    public void testGetProductPriceStub() {
        PricingService service = new PricingService();
        assertEquals(29.99, service.getProductPrice("P001", "A101-01")); 
    }
}