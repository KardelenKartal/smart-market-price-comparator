package util;

/**
 * GeoUtils
 * Iki koordinat arasi mesafe ve yurume suresi hesaplar.
 * Haversine formulunu kullanir (dunyanin yuvarlakligini hesaba katar).
 */
public class GeoUtils {

    // dunya yaricapi (km)
    private static final double EARTH_RADIUS = 6371.0;

    // ortalama yurume hizi (km/h)
    private static final double WALKING_SPEED = 5.0;

    // ================= HAVERSINE =================

    /**
     * Iki koordinat arasi kus ucusu mesafe (km).
     * lat/lng derece cinsinden verilir.
     */
    public static double haversine(double lat1, double lon1, double lat2, double lon2) {

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double radLat1 = Math.toRadians(lat1);
        double radLat2 = Math.toRadians(lat2);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(radLat1) * Math.cos(radLat2)
                 * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

    // ================= YURUME SURESI =================

    /**
     * Mesafeyi (km) dakika cinsinden yurume suresine cevirir.
     * Ortalama 5 km/h yurume hizi kullanir.
     */
    public static double walkingTime(double distanceKm) {
        if (distanceKm <= 0) return 0.0;

        return (distanceKm / WALKING_SPEED) * 60.0;
    }

    // ================= TOPLAM MESAFE =================

    /**
     * Birden fazla nokta arasindaki toplam mesafeyi hesaplar.
     * lats ve lons dizileri ayni uzunlukta olmali.
     * Sirali olarak her nokta arasini toplar.
     */
    public static double totalDistance(double[] lats, double[] lons) {
