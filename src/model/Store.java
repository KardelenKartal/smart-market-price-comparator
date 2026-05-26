package model;

public class Store {
    private String storeId;
    private String name;
    private String chainName;

    public Store() {}

    public Store(String storeId, String name, String chainName) {
        this.storeId = storeId;
        this.name = name;
        this.chainName = chainName;
    }

    public String getStoreId() { return storeId; }
    public void setStoreId(String storeId) { this.storeId = storeId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getChainName() { return chainName; }
    public void setChainName(String chainName) { this.chainName = chainName; }

    @Override
    public String toString() {
        return "Store{storeId='" + storeId + "', name='" + name + "', chainName='" + chainName + "'}";
    }
}
