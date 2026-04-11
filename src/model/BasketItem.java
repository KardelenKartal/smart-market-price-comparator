package model;

public class BasketItem {
    private int basketId;
    private String stockItemId;
    private int quantity;

    public BasketItem() {}

    public BasketItem(int basketId, String stockItemId, int quantity) {
        this.basketId = basketId;
        this.stockItemId = stockItemId;
        this.quantity = quantity;
    }

    public int getBasketId() { return basketId; }
    public void setBasketId(int basketId) { this.basketId = basketId; }

    public String getStockItemId() { return stockItemId; }
    public void setStockItemId(String stockItemId) { this.stockItemId = stockItemId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    @Override
    public String toString() {
        return "BasketItem{basketId=" + basketId + ", stockItemId='" + stockItemId +
               "', quantity=" + quantity + "}";
    }
}
