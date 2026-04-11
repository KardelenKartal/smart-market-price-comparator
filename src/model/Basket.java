package model;

import java.util.ArrayList;
import java.util.List;

public class Basket {
    private int basketId;
    private List<BasketItem> items;

    public Basket() {
        this.items = new ArrayList<>();
    }

    public Basket(int basketId) {
        this.basketId = basketId;
        this.items = new ArrayList<>();
    }

    public int getBasketId() { return basketId; }
    public void setBasketId(int basketId) { this.basketId = basketId; }

    public List<BasketItem> getItems() { return items; }
    public void setItems(List<BasketItem> items) { this.items = items; }

    public void addItem(BasketItem item) { this.items.add(item); }
    public void removeItem(BasketItem item) { this.items.remove(item); }

    public int getItemCount() { return items.size(); }

    @Override
    public String toString() {
        return "Basket{basketId=" + basketId + ", itemCount=" + items.size() + "}";
    }
}
