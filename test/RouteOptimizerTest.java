package test;

import model.*;
import service.route.RouteOptimizer;

import java.util.ArrayList;
import java.util.List;

/**
 * RouteOptimizerTest
 * RouteOptimizer icin temel smoke testleri.
 */
public class RouteOptimizerTest {

    // ================= TEST RUNNER =================

    public static void main(String[] args) {
        System.out.println("=== RouteOptimizer Testleri ===\n");

        testReturnsResult();
        testHasStores();
        testDistance();

        System.out.println("\nTum testler tamamlandi.");
    }

    // ================= SMOKE TESTLERI =================

    private static void testReturnsResult() {
        RouteOptimizer optimizer = new RouteOptimizer();

        List<BasketItem> basket = new ArrayList<>();
        basket.add(new BasketItem(1, "SI0001", 1));

        List<StockItem> stockItems = new ArrayList<>();
        List<StoreLocation> locations = getSampleLocations();

        RouteResult result = optimizer.optimizeRoute(
                basket, stockItems, locations, 40.9755, 29.1498);

        boolean passed = (result != null);

        System.out.println("[testReturnsResult] result != null -> " +
                (passed ? "PASSED" : "FAILED"));
    }

    private static void testHasStores() {
        RouteOptimizer optimizer = new RouteOptimizer();

        RouteResult result = optimizer.optimizeRoute(
                new ArrayList<>(), new ArrayList<>(), getSampleLocations(),
                40.9755, 29.1498);

        boolean passed = (result.getStores() != null && !result.getStores().isEmpty());

        System.out.println("[testHasStores] store listesi bos degil -> " +
                (passed ? "PASSED" : "FAILED"));
    }

    private static void testDistance() {
        RouteOptimizer optimizer = new RouteOptimizer();

        RouteResult result = optimizer.optimizeRoute(
                new ArrayList<>(), new ArrayList<>(), getSampleLocations(),
                40.9755, 29.1498);

        boolean passed = (result.getTotalDistance() > 0.0);

        System.out.println("[testDistance] mesafe > 0 -> " +
                (passed ? "PASSED" : "FAILED"));
    }

    // ================= YARDIMCI =================

    private static List<StoreLocation> getSampleLocations() {
        List<StoreLocation> locs = new ArrayList<>();
        locs.add(new StoreLocation("A101-01", 40.9762, 29.1459, "Kayisdagi Cd. No:48"));
        locs.add(new StoreLocation("MIGROS-01", 40.9835, 29.1462, "Atilla Cd. No:22"));
        return locs;
    }
}
