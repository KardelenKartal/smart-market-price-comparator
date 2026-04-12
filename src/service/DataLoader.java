package service;

import model.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * DATA LOADER
 * Main function: Reads JSON files from src/data/ and maps them to model objects.
 *
 * For usage:
 *   DataLoader loader = new DataLoader();
 *   DataLoader.AppData data = loader.loadAll("W1");
 *
 * Also: 
 *   List<StockItem> items = loader.loadStockItems();
 */
public class DataLoader {

    private static final Path DATA_DIR = Paths.get("src", "data");
    private final JSONParser parser = new JSONParser();

    // CATEGORIES

    public List<Category> loadCategories() {
        List<Category> list = new ArrayList<>();
        JSONArray arr = readArray("categories.json");
        if (arr == null) return list;

        for (Object obj : arr) {
            JSONObject o = (JSONObject) obj;
            list.add(new Category(
                str(o, "categoryId"),
                str(o, "name")
            ));
        }

        System.out.println("[DataLoader] Loaded " + list.size() + " categories.");
        return list;
    }

    // PRODUCTS

    public List<Product> loadProducts() {
        List<Product> list = new ArrayList<>();
        JSONArray arr = readArray("products.json");
        if (arr == null) return list;

        for (Object obj : arr) {
            JSONObject o = (JSONObject) obj;
            list.add(new Product(
                str(o, "productId"),
                str(o, "name"),
                str(o, "categoryId"),
                str(o, "categoryName")
            ));
        }

        System.out.println("[DataLoader] Loaded " + list.size() + " products.");
        return list;
    }

    // STORES

    public List<Store> loadStores() {
        List<Store> list = new ArrayList<>();
        JSONArray arr = readArray("stores.json");
        if (arr == null) return list;

        for (Object obj : arr) {
            JSONObject o = (JSONObject) obj;
            list.add(new Store(
                str(o, "storeId"),
                str(o, "name"),
                str(o, "chainName")
            ));
        }

        System.out.println("[DataLoader] Loaded " + list.size() + " stores.");
        return list;
    }

    // STORE LOCATIONS

    public List<StoreLocation> loadStoreLocations() {
        List<StoreLocation> list = new ArrayList<>();
        JSONArray arr = readArray("store_locations.json");
        if (arr == null) return list;

        for (Object obj : arr) {
            JSONObject o = (JSONObject) obj;
            list.add(new StoreLocation(
                str(o, "storeId"),
                toDouble(o, "latitude"),
                toDouble(o, "longitude"),
                str(o, "address")
            ));
        }

        System.out.println("[DataLoader] Loaded " + list.size() + " store locations.");
        return list;
    }

    // STOCK ITEMS

    public List<StockItem> loadStockItems() {
        List<StockItem> list = new ArrayList<>();
        JSONArray arr = readArray("stock_items.json");
        if (arr == null) return list;

        for (Object obj : arr) {
            JSONObject o = (JSONObject) obj;

            Double discountedPrice = null;
            if (o.get("discountedPrice") != null) {
                discountedPrice = toDouble(o, "discountedPrice");
            }

            list.add(new StockItem(
                str(o, "stockItemId"),
                str(o, "productId"),
                str(o, "productName"),
                str(o, "categoryId"),
                str(o, "category"),
                str(o, "storeId"),
                str(o, "chain"),
                str(o, "brand"),
                toDouble(o, "currentPrice"),
                discountedPrice,
                AvailabilityTag.fromString(str(o, "availabilityTag"))
            ));
        }

        System.out.println("[DataLoader] Loaded " + list.size() + " stock items.");
        return list;
    }

    // PRICE HISTORY

    public List<PriceHistory> loadPriceHistory() {
        List<PriceHistory> list = new ArrayList<>();
        JSONArray arr = readArray("price_history.json");
        if (arr == null) return list;

        for (Object obj : arr) {
            JSONObject o = (JSONObject) obj;
            list.add(new PriceHistory(
                str(o, "historyId"),
                str(o, "stockItemId"),
                str(o, "date"),
                toDouble(o, "price")
            ));
        }

        System.out.println("[DataLoader] Loaded " + list.size() + " price history records.");
        return list;
    }

    // CAMPAIGNS

    public List<Campaign> loadCampaigns() {
        List<Campaign> list = new ArrayList<>();
        JSONArray arr = readArray("campaigns.json");
        if (arr == null) return list;

        for (Object obj : arr) {
            JSONObject o = (JSONObject) obj;
            list.add(new Campaign(
                str(o, "campaignId"),
                str(o, "stockItemId"),
                str(o, "productName"),
                str(o, "chain"),
                str(o, "type"),
                toDouble(o, "discountRate"),
                str(o, "discountType"),
                str(o, "startDate"),
                str(o, "endDate")
            ));
        }

        System.out.println("[DataLoader] Loaded " + list.size() + " campaigns.");
        return list;
    }

    // DISCOUNT APPLICATION

    public void applyDiscounts(List<StockItem> stockItems,
                               List<Campaign> campaigns,
                               String currentWeek) {
        int applied = 0;

        for (Campaign c : campaigns) {
            if (!c.getStartDate().equalsIgnoreCase(currentWeek)) continue;

            for (StockItem si : stockItems) {
                if (si.getStockItemId().equals(c.getStockItemId())
                        && si.getChain().equalsIgnoreCase(c.getChain())) {

                    double discounted = si.getCurrentPrice() *
                            (1.0 - c.getDiscountRate() / 100.0);

                    si.setDiscountedPrice(Math.round(discounted * 100.0) / 100.0);
                    applied++;
                }
            }
        }

        System.out.println("[DataLoader] Applied " + applied +
                " discounts for week: " + currentWeek);
    }

    // LOADING

    public AppData loadAll(String currentWeek) {
        AppData data = new AppData();
        data.categories = loadCategories();
        data.products = loadProducts();
        data.stores = loadStores();
        data.storeLocations = loadStoreLocations();
        data.stockItems = loadStockItems();
        data.priceHistory = loadPriceHistory();
        data.campaigns = loadCampaigns();
        applyDiscounts(data.stockItems, data.campaigns, currentWeek);
        return data;
    }

    // DATA CONTAINER

    public static class AppData {
        public List<Category> categories = new ArrayList<>();
        public List<Product> products = new ArrayList<>();
        public List<Store> stores = new ArrayList<>();
        public List<StoreLocation> storeLocations = new ArrayList<>();
        public List<StockItem> stockItems = new ArrayList<>();
        public List<PriceHistory> priceHistory = new ArrayList<>();
        public List<Campaign> campaigns = new ArrayList<>();
    }

    // HELPER ( PRIVATE)

    private JSONArray readArray(String filename) {
        Path path = DATA_DIR.resolve(filename);

        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            Object parsed = parser.parse(reader);

            if (parsed instanceof JSONArray) {
                return (JSONArray) parsed;
            }

            System.err.println("[DataLoader] Expected JSON array in: " + path);
            return null;

        } catch (Exception e) {
            System.err.println("[DataLoader] Error reading " + path + ": " + e.getMessage());
            return null;
        }
    }

    private double toDouble(JSONObject o, String key) {
        Object val = o.get(key);
        if (val == null) return 0.0;
        if (val instanceof Number) return ((Number) val).doubleValue();

        try {
            return Double.parseDouble(val.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private String str(JSONObject o, String key) {
        Object val = o.get(key);
        return val != null ? val.toString() : "";
    }
}
