package service.route;

import model.*;
import util.GeoUtils;

import java.util.*;


public class RouteOptimizer {

    public RouteResult optimizeRoute(List<BasketItem> basketItems,
                                     List<StockItem>  stockItems,
                                     List<StoreLocation> storeLocations,
                                     double startLat, double startLon) {

        if (basketItems == null || basketItems.isEmpty()) return emptyResult();

        Map<String, List<StockItem>> stockByChain = new HashMap<>();
        for (StockItem s : nvl(stockItems)) {
            String chain = normalize(s.getChain());
            if (!chain.isEmpty())
                stockByChain.computeIfAbsent(chain, k -> new ArrayList<>()).add(s);
        }

        Set<String> neededChains = new LinkedHashSet<>();
        double totalCost = 0;

        for (BasketItem bi : basketItems) {
            String pid       = bi.getStockItemId(); 
            String bestChain = "";
            double bestPrice = Double.MAX_VALUE;

            for (Map.Entry<String, List<StockItem>> e : stockByChain.entrySet()) {
                for (StockItem s : e.getValue()) {
                    if (s.getProductId() != null && s.getProductId().equalsIgnoreCase(pid)) {
                        double price = s.getEffectivePrice();
                        if (price < bestPrice) { bestPrice = price; bestChain = e.getKey(); }
                    }
                }
            }

            if (!bestChain.isEmpty()) {
                neededChains.add(bestChain);
                if (bestPrice < Double.MAX_VALUE) totalCost += bestPrice * bi.getQuantity();
            }
        }

        if (neededChains.isEmpty()) return emptyResult();

        List<StoreLocation> waypoints = new ArrayList<>();
        for (String chain : neededChains) {
            StoreLocation nearest = null;
            double minDist = Double.MAX_VALUE;
            for (StoreLocation loc : nvl(storeLocations)) {
                if (chainFromStoreId(loc.getStoreId()).equals(chain)) {
                    double d = GeoUtils.haversine(startLat, startLon,
                                                  loc.getLatitude(), loc.getLongitude());
                    if (d < minDist) { minDist = d; nearest = loc; }
                }
            }
            if (nearest != null) waypoints.add(nearest);
        }

        if (waypoints.isEmpty()) return emptyResult();

        // 4. En kısa rotayı bul (brute-force)
        List<StoreLocation> bestOrder = findShortestRoute(startLat, startLon, waypoints);
        double totalDist = routeDistance(startLat, startLon, bestOrder);

        // 5. Sonuç — RouteResultStore(String storeId, List<String> productIds)
        List<RouteResultStore> resultStores = new ArrayList<>();
        for (int i = 0; i < bestOrder.size(); i++) {
            resultStores.add(new RouteResultStore(i + 1, bestOrder.get(i).getStoreId()));
        }

        RouteResult result = new RouteResult(0, totalDist, resultStores.size(), totalCost);
        result.setStores(resultStores);
        return result;
    }

    private List<StoreLocation> findShortestRoute(double startLat, double startLon,
                                                   List<StoreLocation> points) {
        if (points.size() <= 1) return new ArrayList<>(points);

        List<StoreLocation> best     = null;
        double              bestDist = Double.MAX_VALUE;

        for (List<StoreLocation> perm : permutations(points)) {
            double d = routeDistance(startLat, startLon, perm);
            if (d < bestDist) { bestDist = d; best = perm; }
        }
        return best != null ? best : new ArrayList<>(points);
    }

    private double routeDistance(double startLat, double startLon, List<StoreLocation> stops) {
        double total = 0, curLat = startLat, curLon = startLon;
        for (StoreLocation s : stops) {
            total  += GeoUtils.haversine(curLat, curLon, s.getLatitude(), s.getLongitude());
            curLat  = s.getLatitude();
            curLon  = s.getLongitude();
        }
        return total;
    }

    private List<List<StoreLocation>> permutations(List<StoreLocation> list) {
        List<List<StoreLocation>> result = new ArrayList<>();
        StoreLocation[] arr = list.toArray(new StoreLocation[0]);
        int[] c = new int[arr.length];
        result.add(new ArrayList<>(Arrays.asList(arr)));
        int i = 0;
        while (i < arr.length) {
            if (c[i] < i) {
                StoreLocation tmp;
                if (i % 2 == 0) { tmp = arr[0];    arr[0]    = arr[i]; arr[i] = tmp; }
                else             { tmp = arr[c[i]]; arr[c[i]] = arr[i]; arr[i] = tmp; }
                result.add(new ArrayList<>(Arrays.asList(arr)));
                c[i]++; i = 0;
            } else { c[i] = 0; i++; }
        }
        return result;
    }

    private String chainFromStoreId(String storeId) {
        if (storeId == null) return "";
        int dash = storeId.lastIndexOf('-');
        String prefix = (dash > 0 ? storeId.substring(0, dash) : storeId).toUpperCase();
        switch (prefix) {
            case "CF":     return "carefour";
            case "A101":   return "a101";
            case "BIM":    return "bim";
            case "SOK":    return "sok";
            case "MIGROS": return "migros";
            default:       return normalize(prefix);
        }
    }

    private String normalize(String s) {
        if (s == null || s.isBlank()) return "";
        return s.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    private RouteResult emptyResult() {
        return new RouteResult(0, 0, 0, 0);
    }

    private <T> List<T> nvl(List<T> l) { return l != null ? l : new ArrayList<>(); }
}