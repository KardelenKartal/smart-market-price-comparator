package test;

import util.GeoUtils;

/**
 * GeoUtilsTest
 * Haversine mesafe hesabinin temel testleri.
 */
public class GeoUtilsTest {

    // ================= TEST RUNNER =================

    public static void main(String[] args) {
        System.out.println("=== GeoUtils Testleri ===\n");

        testHaversineBasic();
        testHaversineSamePoint();
        testWalkingTime();
        testTotalDistance();

        System.out.println("\nTum testler tamamlandi.");
    }

    // ================= HAVERSINE TESTLERI =================

    private static void testHaversineBasic() {
        double dist = GeoUtils.haversine(40.9762, 29.1459, 40.9835, 29.1462);

        boolean passed = (dist > 0.7 && dist < 0.9);

        System.out.println("[testHaversineBasic] mesafe = " +
                Math.round(dist * 1000.0) / 1000.0 + " km -> " +
                (passed ? "PASSED" : "FAILED"));
    }

    private static void testHaversineSamePoint() {
        double dist = GeoUtils.haversine(40.9762, 29.1459, 40.9762, 29.1459);

        boolean passed = (dist == 0.0);

        System.out.println("[testHaversineSamePoint] mesafe = " + dist +
                " km -> " + (passed ? "PASSED" : "FAILED"));
    }

    // ================= YURUME SURESI TESTI =================

    private static void testWalkingTime() {
        double time = GeoUtils.walkingTime(1.0);

        boolean passed = (time == 12.0);

        System.out.println("[testWalkingTime] 1 km = " + time +
                " dk -> " + (passed ? "PASSED" : "FAILED"));
    }

    // ================= TOPLAM MESAFE TESTI =================

    private static void testTotalDistance() {
        double[] lats = {40.9755, 40.9762, 40.9835};
        double[] lons = {29.1498, 29.1459, 29.1462};

        double total = GeoUtils.totalDistance(lats, lons);

        boolean passed = (total > 0.0);

        System.out.println("[testTotalDistance] toplam = " +
                Math.round(total * 1000.0) / 1000.0 + " km -> " +
                (passed ? "PASSED" : "FAILED"));
    }
}

