package model;

public class Product {
    private String productId;
    private String name;
    private String categoryId;
    private String categoryName;

    public Product() {}

    public Product(String productId, String name, String categoryId, String categoryName) {
        this.productId = productId;
        this.name = name;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    @Override
    public String toString() {
        return "Product{productId='" + productId + "', name='" + name + "', categoryId='" + categoryId + "'}";
    }
}
