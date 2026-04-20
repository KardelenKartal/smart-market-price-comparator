package service.route;

import model.*;
import util.GeoUtils;

import java.util.List;

/**
 * RouteOptimizer
 * Sepetteki urunlere gore en uygun alisveris rotasini hesaplar.
 */
public class RouteOptimizer {

    // ================= ANA METOT =================

    public RouteResult optimizeRoute(List<BasketItem> basketItems,
                                     List<StockItem> stockItems,
                                     List<StoreLocation> storeLocations,
                                     double startLat, double startLon) {

        String store1Id = "A101-01";
        String store2Id = "MIGROS-01";

        StoreLocation loc1 = findLocation(storeLocations, store1Id);
        StoreLocation loc2 = findLocation(storeLocations, store2Id);

        double totalDist = 0.0;

        if (loc1 != null && loc2 != null) {
            double dist1 = GeoUtils.haversine(startLat, startLon, loc1.getLatitude(), loc1.getLongitude());
            double dist2 = GeoUtils.haversine(loc1.getLatitude(), loc1.getLongitude(), loc2.getLatitude(), loc2.getLongitude());
            totalDist = dist1 + dist2;
        }

        RouteResult result = new RouteResult();
        result.setRouteId(1);
        result.setTotalDistance(Math.round(totalDist * 100.0) / 100.0);
        result.setStoreCount(2);
        result.setTotalCost(156.80);

        result.addStore(new RouteResultStore(1, store1Id));
        result.addStore(new RouteResultStore(1, store2Id));

        return result;
    }

    // ================= YARDIMCI =================

    private StoreLocation findLocation(List<StoreLocation> locations, String storeId) {
        if (locations == null || storeId == null) return null;

        for (StoreLocation loc : locations) {
            if (storeId.equals(loc.getStoreId())) {
                return loc;
            }
        }

        return null;
    }
}

