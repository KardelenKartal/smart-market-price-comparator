package model;

public class StoreLocation {
    private String storeId;
    private double latitude;
    private double longitude;
    private String address;

    public StoreLocation() {}

    public StoreLocation(String storeId, double latitude, double longitude, String address) {
        this.storeId = storeId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
    }

    public String getStoreId() { return storeId; }
    public void setStoreId(String storeId) { this.storeId = storeId; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    @Override
    public String toString() {
        return "StoreLocation{storeId='" + storeId + "', lat=" + latitude + ", lng=" + longitude + "}";
    }
}
