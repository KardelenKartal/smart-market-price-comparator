package model;

public class StockItem {
    private String stockItemId;
    private String productId;
    private String productName;
    private String categoryId;
    private String category;
    private String storeId;
    private String chain;
    private String brand;
    private double currentPrice;
    private Double discountedPrice;
    private AvailabilityTag availabilityTag;

    public StockItem() {}

    public StockItem(String stockItemId, String productId, String productName,
                     String categoryId, String category, String storeId, String chain,
                     String brand, double currentPrice, Double discountedPrice,
                     AvailabilityTag availabilityTag) {
        this.stockItemId = stockItemId;
        this.productId = productId;
        this.productName = productName;
        this.categoryId = categoryId;
        this.category = category;
        this.storeId = storeId;
        this.chain = chain;
        this.brand = brand;
        this.currentPrice = currentPrice;
        this.discountedPrice = discountedPrice;
        this.availabilityTag = availabilityTag;
    }

    public String getStockItemId() { return stockItemId; }
    public void setStockItemId(String stockItemId) { this.stockItemId = stockItemId; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getStoreId() { return storeId; }
    public void setStoreId(String storeId) { this.storeId = storeId; }

    public String getChain() { return chain; }
    public void setChain(String chain) { this.chain = chain; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public double getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(double currentPrice) { this.currentPrice = currentPrice; }

    public Double getDiscountedPrice() { return discountedPrice; }
    public void setDiscountedPrice(Double discountedPrice) { this.discountedPrice = discountedPrice; }

    public AvailabilityTag getAvailabilityTag() { return availabilityTag; }
    public void setAvailabilityTag(AvailabilityTag availabilityTag) { this.availabilityTag = availabilityTag; }

    public double getEffectivePrice() {
        return (discountedPrice != null) ? discountedPrice : currentPrice;
    }

    @Override
    public String toString() {
        return "StockItem{id='" + stockItemId + "', product='" + productName +
               "', chain='" + chain + "', brand='" + brand + "', price=" + currentPrice + "}";
    }
}
