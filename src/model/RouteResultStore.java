package model;

public class RouteResultStore {
    private int routeId;
    private String storeId;

    public RouteResultStore() {}

    public RouteResultStore(int routeId, String storeId) {
        this.routeId = routeId;
        this.storeId = storeId;
    }

    public int getRouteId() { return routeId; }
    public void setRouteId(int routeId) { this.routeId = routeId; }

    public String getStoreId() { return storeId; }
    public void setStoreId(String storeId) { this.storeId = storeId; }

    @Override
    public String toString() {
        return "RouteResultStore{routeId=" + routeId + ", storeId='" + storeId + "'}";
    }
}
