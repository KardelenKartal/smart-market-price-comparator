package test;

import model.*;
import service.route.RouteOptimizer;
import service.route.RouteStrategy;

import java.util.ArrayList;
import java.util.List;

/**
 * RouteOptimizerTest
 * RouteOptimizer stub'inin temel smoke testleri.
 */
public class RouteOptimizerTest {

    // ================= TEST RUNNER =================

    public static void main(String[] args) {
        System.out.println("=== RouteOptimizer Testleri ===\n");

        testStubReturnsResult();
        testStubHasStores();
        testStubDistance();

        System.out.println("\nTum testler tamamlandi.");
    }

    // ================= SMOKE TESTLERI =================

    /**
     * optimizeRoute null donmemeli.
     */
    private static void testStubReturnsResult() {
        RouteOptimizer optimizer = new RouteOptimizer();

        List<BasketItem> basket = new ArrayList<>();
        basket.add(new BasketItem(1, "SI0001", 1));

        List<StockItem> stockItems = new ArrayList<>();
        List<StoreLocation> locations = getSampleLocations();

        // Yeditepe Ust Kapi koordinatlari
        double startLat = 40.9755;
        double startLon = 29.1498;

        RouteResult result = optimizer.optimizeRoute(
                basket, stockItems, locations, startLat, startLon,
                RouteStrategy.CHEAPEST);

        boolean passed = (result != null);

        System.out.println("[testStubReturnsResult] result != null -> " +
                (passed ? "PASSED" : "FAILED"));
    }

    /**
     * Rota en az 1 magaza icermeli.
     */
    private static void testStubHasStores() {
        RouteOptimizer optimizer = new RouteOptimizer();

        RouteResult result = optimizer.optimizeRoute(
                new ArrayList<>(), new ArrayList<>(), getSampleLocations(),
                40.9755, 29.1498, RouteStrategy.CHEAPEST);

        boolean passed = (result.getStores() != null && !result.getStores().isEmpty());

        System.out.println("[testStubHasStores] store listesi bos degil -> " +
                (passed ? "PASSED" : "FAILED"));
    }

    /**
     * Toplam mesafe 0'dan buyuk olmali.
     */
    private static void testStubDistance() {
        RouteOptimizer optimizer = new RouteOptimizer();

        RouteResult result = optimizer.optimizeRoute(
                new ArrayList<>(), new ArrayList<>(), getSampleLocations(),
                40.9755, 29.1498, RouteStrategy.FEWEST_STOPS);

        boolean passed = (result.getTotalDistance() > 0.0);

        System.out.println("[testStubDistance] mesafe > 0 -> " +
                (passed ? "PASSED" : "FAILED"));
    }

    // ================= YARDIMCI =================

    /**
     * Test icin ornek magaza konumlari.
     */
    private static List<StoreLocation> getSampleLocations() {
        List<StoreLocation> locs = new ArrayList<>();
        locs.add(new StoreLocation("A101-01", 40.9762, 29.1459, "Kayisdagi Cd. No:48"));
        locs.add(new StoreLocation("MIGROS-01", 40.9835, 29.1462, "Atilla Cd. No:22"));
        return locs;
    }
}
