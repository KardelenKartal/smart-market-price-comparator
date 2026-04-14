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

    /**
     * A101-01 ile MIGROS-01 arasi mesafe testi.
     * A101-01:   40.9762, 29.1459
     * MIGROS-01: 40.9835, 29.1462
     * Beklenen: ~0.81 km
     */
    private static void testHaversineBasic() {
        double dist = GeoUtils.haversine(40.9762, 29.1459, 40.9835, 29.1462);

        // 0.7 - 0.9 km arasinda olmali
        boolean passed = (dist > 0.7 && dist < 0.9);

        System.out.println("[testHaversineBasic] mesafe = " +
                Math.round(dist * 1000.0) / 1000.0 + " km -> " +
                (passed ? "PASSED" : "FAILED"));
    }

    /**
     * Ayni nokta icin mesafe 0 olmali.
     */
    private static void testHaversineSamePoint() {
        double dist = GeoUtils.haversine(40.9762, 29.1459, 40.9762, 29.1459);

        boolean passed = (dist == 0.0);

        System.out.println("[testHaversineSamePoint] mesafe = " + dist +
                " km -> " + (passed ? "PASSED" : "FAILED"));
    }

    // ================= YURUME SURESI TESTI =================

    /**
     * 1 km icin yurume suresi 12 dakika olmali (5 km/h hizla).
     */
    private static void testWalkingTime() {
        double time = GeoUtils.walkingTime(1.0);

        boolean passed = (time == 12.0);

        System.out.println("[testWalkingTime] 1 km = " + time +
                " dk -> " + (passed ? "PASSED" : "FAILED"));
    }

    // ================= TOPLAM MESAFE TESTI =================

    /**
     * 3 noktali rota toplam mesafe testi.
     * Yeditepe Ust Kapi -> A101-01 -> MIGROS-01
     */
    private static void testTotalDistance() {
        double[] lats = {40.9755, 40.9762, 40.9835};
        double[] lons = {29.1498, 29.1459, 29.1462};

        double total = GeoUtils.totalDistance(lats, lons);

        // toplam mesafe 0'dan buyuk olmali
        boolean passed = (total > 0.0);

        System.out.println("[testTotalDistance] toplam = " +
                Math.round(total * 1000.0) / 1000.0 + " km -> " +
                (passed ? "PASSED" : "FAILED"));
    }
}
