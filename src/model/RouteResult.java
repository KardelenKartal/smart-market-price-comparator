package model;

import java.util.ArrayList;
import java.util.List;

public class RouteResult {
    private int routeId;
    private double totalDistance;
    private int storeCount;
    private double totalCost;
    private List<RouteResultStore> stores;

    public RouteResult() {
        this.stores = new ArrayList<>();
    }

    public RouteResult(int routeId, double totalDistance, int storeCount, double totalCost) {
        this.routeId = routeId;
        this.totalDistance = totalDistance;
        this.storeCount = storeCount;
        this.totalCost = totalCost;
        this.stores = new ArrayList<>();
    }

    public int getRouteId() { return routeId; }
    public void setRouteId(int routeId) { this.routeId = routeId; }

    public double getTotalDistance() { return totalDistance; }
    public void setTotalDistance(double totalDistance) { this.totalDistance = totalDistance; }

    public int getStoreCount() { return storeCount; }
    public void setStoreCount(int storeCount) { this.storeCount = storeCount; }

    public double getTotalCost() { return totalCost; }
    public void setTotalCost(double totalCost) { this.totalCost = totalCost; }

    public List<RouteResultStore> getStores() { return stores; }
    public void setStores(List<RouteResultStore> stores) { this.stores = stores; }

    public void addStore(RouteResultStore store) { this.stores.add(store); }

    @Override
    public String toString() {
        return "RouteResult{routeId=" + routeId + ", totalDistance=" + totalDistance +
               ", storeCount=" + storeCount + ", totalCost=" + totalCost + "}";
    }
}
