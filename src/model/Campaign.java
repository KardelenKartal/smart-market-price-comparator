package model;

public class Campaign {
    private String campaignId;
    private String stockItemId;
    private String productName;
    private String chain;
    private String type;
    private double discountRate;
    private String discountType;
    private String startDate;
    private String endDate;

    public Campaign() {}

    public Campaign(String campaignId, String stockItemId, String productName, String chain,
                    String type, double discountRate, String discountType,
                    String startDate, String endDate) {
        this.campaignId = campaignId;
        this.stockItemId = stockItemId;
        this.productName = productName;
        this.chain = chain;
        this.type = type;
        this.discountRate = discountRate;
        this.discountType = discountType;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getCampaignId() { return campaignId; }
    public void setCampaignId(String campaignId) { this.campaignId = campaignId; }

    public String getStockItemId() { return stockItemId; }
    public void setStockItemId(String stockItemId) { this.stockItemId = stockItemId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getChain() { return chain; }
    public void setChain(String chain) { this.chain = chain; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getDiscountRate() { return discountRate; }
    public void setDiscountRate(double discountRate) { this.discountRate = discountRate; }

    public String getDiscountType() { return discountType; }
    public void setDiscountType(String discountType) { this.discountType = discountType; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    @Override
    public String toString() {
        return "Campaign{campaignId='" + campaignId + "', stockItemId='" + stockItemId +
               "', discountRate=" + discountRate + "%, startDate='" + startDate + "'}";
    }
}
