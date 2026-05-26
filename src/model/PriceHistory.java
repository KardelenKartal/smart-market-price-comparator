package model;

public class PriceHistory {
    private String historyId;
    private String stockItemId;
    private String date;
    private double price;

    public PriceHistory() {}

    public PriceHistory(String historyId, String stockItemId, String date, double price) {
        this.historyId = historyId;
        this.stockItemId = stockItemId;
        this.date = date;
        this.price = price;
    }

    public String getHistoryId() { return historyId; }
    public void setHistoryId(String historyId) { this.historyId = historyId; }

    public String getStockItemId() { return stockItemId; }
    public void setStockItemId(String stockItemId) { this.stockItemId = stockItemId; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    @Override
    public String toString() {
        return "PriceHistory{historyId='" + historyId + "', stockItemId='" + stockItemId +
               "', date='" + date + "', price=" + price + "}";
    }
}
