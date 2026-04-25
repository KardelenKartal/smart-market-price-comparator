package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;

import model.Product;
import model.StockItem;
import model.Store;
import model.Campaign;
import model.PriceHistory;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class BasketScreen {

    private Stage stage;
    private Scene previousScene;

    private List<Product>      basket;
    private List<StockItem>    stockItems;
    private List<Store>        stores;
    private List<Campaign>     campaigns;
    private List<PriceHistory> allPriceHistories;

    private static Map<String, Integer>   quantities        = new LinkedHashMap<>();
    private static Map<String, String>    selectedChain     = new LinkedHashMap<>();
    private static Map<String, StockItem> selectedVariant   = new LinkedHashMap<>();
    private static Map<String, Integer>   variantQuantities = new LinkedHashMap<>();

    private Map<String, List<StockItem>> stockByChain   = new HashMap<>();
    private Map<String, List<StockItem>> stockByStoreId = new HashMap<>();

    private VBox  itemsContainer;
    private Label totalLabel;
    private VBox  suggestionBox;

    public BasketScreen(List<Product> basket,
                        List<StockItem> stockItems,
                        List<Store> stores,
                        List<Campaign> campaigns) {
        this.basket     = nvl(basket);
        this.stockItems = nvl(stockItems);
        this.stores     = nvl(stores);
        this.campaigns  = nvl(campaigns);

        for (Product p : this.basket) {
            quantities.put(p.getProductId(), 1);
            selectedChain.put(p.getProductId(), "");
        }
    }

    public void setAllPriceHistories(List<PriceHistory> histories) { this.allPriceHistories = histories; }
    public void setPreviousScene(Scene scene) { this.previousScene = scene; }

    public void start(Stage stage) {
        this.stage = stage;
        buildIndex();

        StackPane root = new StackPane();
        setBackground(root);

        VBox page = new VBox(16);
        page.setPadding(new Insets(24));
        page.setAlignment(Pos.TOP_CENTER);
        page.setStyle("-fx-background-color: rgba(255,255,255,0.45); -fx-background-radius: 20;");

        Label title = new Label("MY BASKET");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));
        title.setTextFill(Color.DARKBLUE);
        page.getChildren().add(title);

        itemsContainer = new VBox(10);
        itemsContainer.setFillWidth(true);
        rebuildItems();

        ScrollPane itemsScroll = new ScrollPane(itemsContainer);
        itemsScroll.setFitToWidth(true);
        itemsScroll.setPrefHeight(380);
        itemsScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        page.getChildren().add(itemsScroll);

        suggestionBox = new VBox(6);
        suggestionBox.setPadding(new Insets(14));
        suggestionBox.setStyle("-fx-background-color: #e8f5e9; -fx-background-radius: 10; -fx-border-color: #a5d6a7; -fx-border-radius: 10; -fx-border-width: 1.5;");
        rebuildSuggestion();
        page.getChildren().add(suggestionBox);

        totalLabel = new Label();
        totalLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        totalLabel.setTextFill(Color.web("#0d47a1"));
        rebuildTotal();
        page.getChildren().add(totalLabel);

        // Price History butonu
        Button btnHistory = new Button("PRICE HISTORY");
        btnHistory.setMaxWidth(Double.MAX_VALUE);
        btnHistory.setPrefHeight(44);
        btnHistory.setStyle("-fx-background-color: #5e818c; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 8;");
        btnHistory.setOnMouseEntered(e -> { btnHistory.setScaleX(1.06); btnHistory.setScaleY(1.06); });
        btnHistory.setOnMouseExited(e -> { btnHistory.setScaleX(1.0); btnHistory.setScaleY(1.0); });
        btnHistory.setOnAction(e -> {
            try {
                // variantQuantities'de qty>0 olan TÜM varyantları geç
                List<StockItem> selected = new ArrayList<StockItem>();
                for (StockItem s : stockItems) {
                    if (variantQuantities.getOrDefault(s.getStockItemId(), 0) > 0) {
                        selected.add(s);
                    }
                }
                Scene previousScene = this.stage.getScene();
                List<PriceHistory> ph = (allPriceHistories != null)
                    ? allPriceHistories : new ArrayList<PriceHistory>();
                PriceHistoryScreen historyScreen = new PriceHistoryScreen(
                    basket, stockItems, stores, ph, selected
                );
                historyScreen.setPreviousScene(previousScene);
                historyScreen.start(this.stage);
            } catch (Exception ex) { ex.printStackTrace(); }
        });
        page.getChildren().add(btnHistory);

        Button backBtn = new Button("< BACK TO SEARCH");
        ScaleTransition grow = new ScaleTransition(Duration.millis(150), backBtn);
        ScaleTransition shrink = new ScaleTransition(Duration.millis(150), backBtn);
        backBtn.setOnMouseEntered(e -> { grow.setToX(1.08); grow.setToY(1.08); grow.playFromStart(); });
        backBtn.setOnMouseExited(e -> { shrink.setToX(1.0); shrink.setToY(1.0); shrink.playFromStart(); });
        backBtn.setMaxWidth(Double.MAX_VALUE);
        backBtn.setPrefHeight(44);
        backBtn.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-font-size: 14; -fx-font-weight: bold; -fx-background-radius: 8;");
        backBtn.setOnMouseEntered(e -> {
            backBtn.setScaleX(1.08); backBtn.setScaleY(1.08);
            backBtn.setStyle("-fx-background-color: #b71c1c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");
        });
        backBtn.setOnMouseExited(e -> {
            backBtn.setScaleX(1.0); backBtn.setScaleY(1.0);
            backBtn.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");
        });
        backBtn.setOnAction(e -> {
            if (previousScene != null) { stage.setScene(previousScene); stage.show(); }
            else stage.close();
        });
        page.getChildren().add(backBtn);

        ScrollPane outer = new ScrollPane(page);
        outer.setFitToWidth(true);
        outer.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        root.getChildren().add(outer);

        stage.setScene(new Scene(root, 1000, 750));
        stage.show();
    }

    private void rebuildItems() {
        itemsContainer.getChildren().clear();

        for (Product product : basket) {
            String pId     = product.getProductId();
            String pName   = product.getName();
            int    qty     = quantities.getOrDefault(pId, 1);
            String curChain = selectedChain.getOrDefault(pId, "");

            List<String> chainNames = getDistinctChains();

            VBox card = new VBox(8);
            card.setPadding(new Insets(12));
            card.setStyle("-fx-background-color:#e8f5e9; -fx-border-color: #cccccc; -fx-border-width: 1.5; -fx-background-radius: 10; -fx-border-radius: 10;");

            HBox topRow = new HBox(12);
            topRow.setAlignment(Pos.CENTER_LEFT);

            Label nameLbl = new Label(pName);
            nameLbl.setFont(Font.font("System", FontWeight.BOLD, 15));
            nameLbl.setTextFill(Color.BLACK);
            nameLbl.setMinWidth(200);

            int totalSelected = 0;
            for (StockItem s : stockItems) {
                if (s.getProductId().equalsIgnoreCase(pId))
                    totalSelected += variantQuantities.getOrDefault(s.getStockItemId(), 0);
            }
            Label totalQtyLbl = new Label(totalSelected > 0 ? "Total number of products: " + totalSelected + "" : "");
            totalQtyLbl.setFont(Font.font("System", FontWeight.BOLD, 13));
            totalQtyLbl.setTextFill(Color.web("#1565c0"));

            topRow.getChildren().addAll(nameLbl, totalQtyLbl);
            card.getChildren().add(topRow);

            HBox chainRow = new HBox(8);
            chainRow.setAlignment(Pos.CENTER_LEFT);
            Label chainLbl = new Label("Mağaza:");
            chainLbl.setFont(Font.font("System", 12));
            chainLbl.setTextFill(Color.GREEN);
            chainRow.getChildren().add(chainLbl);

            String cheapestChain = getCheapestChainForProduct(pId);

            for (String chain : chainNames) {
                List<StockItem> chainVariants = findAllByChain(normalize(chain), pId);
                if (chainVariants.isEmpty()) continue;

                double minPrice = chainVariants.get(0).getEffectivePrice();
                boolean isSelected = chain.equals(curChain);
                boolean isCheapest = chain.equals(cheapestChain);

                String chainNameColor, chainPriceColor, chainBg, chainBorder;
                if (isSelected) {
                    chainBg = "#1565c0"; chainBorder = "#0d47a1";
                    chainNameColor = "white"; chainPriceColor = "white";
                } else if (isCheapest) {
                    chainBg = "#e3f2fd"; chainBorder = "#1565c0";
                    chainNameColor = "#0d47a1"; chainPriceColor = "#1565c0";
                } else {
                    chainBg = "#f5f5f5"; chainBorder = "#bbbbbb";
                    chainNameColor = "#333333"; chainPriceColor = "#2e7d32";
                }

                VBox chainBtnContent = new VBox(2);
                chainBtnContent.setAlignment(Pos.CENTER);
                Label cName = new Label(chain);
                cName.setFont(Font.font("System", FontWeight.BOLD, 11));
                cName.setTextFill(Color.web(chainNameColor));
                Label cPrice = new Label(String.format("%.2f TL", minPrice));
                cPrice.setFont(Font.font("System", FontWeight.BOLD, 11));
                cPrice.setTextFill(Color.web(chainPriceColor));
                chainBtnContent.getChildren().addAll(cName, cPrice);

                Button chainBtn = new Button();
                chainBtn.setGraphic(chainBtnContent);
                chainBtn.setPrefWidth(110); chainBtn.setPrefHeight(50);
                chainBtn.setStyle(String.format(
                    "-fx-background-color:%s; -fx-background-radius:6; -fx-border-color:%s; -fx-border-radius:6; -fx-border-width:2;",
                    chainBg, chainBorder));
                chainBtn.setOnAction(e -> {
                    selectedChain.put(pId, chain);
                    selectedVariant.remove(pId); 
                    rebuildItems(); rebuildTotal();
                });
                chainRow.getChildren().add(chainBtn);
            }
            card.getChildren().add(chainRow);

            if (!curChain.isEmpty()) {
                List<StockItem> variants = findAllByChain(normalize(curChain), pId);
                if (!variants.isEmpty()) {
                    double cheapestVariantPrice = variants.get(0).getEffectivePrice();
                    StockItem curSelected = selectedVariant.get(pId);

                    VBox variantBox = new VBox(4);
                    variantBox.setPadding(new Insets(6, 0, 2, 4));

                    Label variantHeader = new Label("Variants in " + curChain + ":");
                    variantHeader.setFont(Font.font("System", FontWeight.BOLD, 11));
                    variantHeader.setTextFill(Color.web("#4a148c"));
                    variantBox.getChildren().add(variantHeader);

                    for (StockItem v : variants) {
                        String brand    = v.getBrand() != null ? v.getBrand() : "?";
                        double vPrice   = v.getEffectivePrice();
                        String sid      = v.getStockItemId();
                        int    vQty     = variantQuantities.getOrDefault(sid, 0);
                        boolean isCheapestVariant = Math.abs(vPrice - cheapestVariantPrice) < 0.01;
                        boolean hasQty  = vQty > 0;

                        HBox varRow = new HBox(8);
                        varRow.setAlignment(Pos.CENTER_LEFT);
                        varRow.setPadding(new Insets(3, 4, 3, 4));

                        // Ürün label
                        Label vLbl = new Label(String.format("%-28s  %.2f TL", brand, vPrice));
                        vLbl.setFont(Font.font("Monospaced", FontWeight.BOLD, 12));
                        vLbl.setMinWidth(320);

                        if (hasQty) {
                            varRow.setStyle("-fx-background-color:#ede7f6; -fx-background-radius:4; -fx-border-color:#6a1b9a; -fx-border-radius:4; -fx-border-width:1.5;");
                            vLbl.setTextFill(Color.web("#4a148c"));
                        } else if (isCheapestVariant) {
                            varRow.setStyle("-fx-background-color:#e8f5e9; -fx-background-radius:4; -fx-border-color:#388e3c; -fx-border-radius:4; -fx-border-width:1;");
                            vLbl.setTextFill(Color.web("#1b5e20"));
                        } else {
                            varRow.setStyle("-fx-background-color:transparent;");
                            vLbl.setTextFill(Color.web("#333333"));
                        }

                        // + - butonları
                        Button vMinus = new Button("−");
                        vMinus.setPrefSize(26, 26);
                        vMinus.setStyle("-fx-background-color:#ef9a9a; -fx-font-weight:bold; -fx-background-radius:5;");
                        vMinus.setVisible(hasQty);
                        vMinus.setManaged(hasQty);

                        Label vQtyLbl = new Label(String.valueOf(vQty));
                        vQtyLbl.setFont(Font.font("System", FontWeight.BOLD, 13));
                        vQtyLbl.setMinWidth(20);
                        vQtyLbl.setAlignment(Pos.CENTER);
                        vQtyLbl.setVisible(hasQty);
                        vQtyLbl.setManaged(hasQty);

                        Button vPlus = new Button("+");
                        vPlus.setPrefSize(26, 26);
                        vPlus.setStyle("-fx-background-color:#a5d6a7; -fx-font-weight:bold; -fx-background-radius:5;");

                        final StockItem finalV = v;
                        vPlus.setOnAction(e -> {
                            variantQuantities.put(sid, variantQuantities.getOrDefault(sid, 0) + 1);
                            selectedVariant.put(pId, finalV);
                            updateTopQty(pId);
                            rebuildItems(); rebuildTotal(); rebuildSuggestion();
                        });
                        vMinus.setOnAction(e -> {
                            int cur = variantQuantities.getOrDefault(sid, 0);
                            if (cur > 1) {
                                variantQuantities.put(sid, cur - 1);
                            } else {
                                variantQuantities.remove(sid);
                                // Bu tek seçiliyse selectedVariant'ı temizle
                                if (curSelected != null && curSelected.getStockItemId().equals(sid)) {
                                    selectedVariant.remove(pId);
                                }
                            }
                            updateTopQty(pId);
                            rebuildItems(); rebuildTotal(); rebuildSuggestion();
                        });

                        varRow.getChildren().addAll(vLbl, vMinus, vQtyLbl, vPlus);
                        variantBox.getChildren().add(varRow);
                    }

                    // Özet — seçili varyantların toplam tutarı
                    double lineTotal = 0;
                    StringBuilder summaryStr = new StringBuilder();
                    for (StockItem v : variants) {
                        int vq = variantQuantities.getOrDefault(v.getStockItemId(), 0);
                        if (vq > 0) {
                            lineTotal += v.getEffectivePrice() * vq;
                            summaryStr.append(vq).append("x ").append(v.getBrand()).append("  ");
                        }
                    }
                    if (lineTotal > 0) {
                        Label lineLabel = new Label(
                            String.format("%.2f TL  [%s]  (%s)", lineTotal, curChain, summaryStr.toString().trim()));
                        lineLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
                        lineLabel.setTextFill(Color.web("#1565c0"));
                        variantBox.getChildren().add(lineLabel);
                    }

                    card.getChildren().add(variantBox);
                }
            } else {
                Label hintLbl = new Label("Choose a chain from above");
                hintLbl.setFont(Font.font("System", 12));
                hintLbl.setTextFill(Color.GRAY);
                card.getChildren().add(hintLbl);
            }

            itemsContainer.getChildren().add(card);
        }
    }

    private void updateTopQty(String pId) {

        int total = 0;
        for (StockItem s : stockItems) {
            if (s.getProductId().equalsIgnoreCase(pId)) {
                total += variantQuantities.getOrDefault(s.getStockItemId(), 0);
            }
        }
        quantities.put(pId, Math.max(total, 1));
    }

    private void rebuildTotal() {
        double total = 0;
        for (Product product : basket) {
            String pId = product.getProductId();
       
            for (StockItem s : stockItems) {
                if (s.getProductId().equalsIgnoreCase(pId)) {
                    int vqty = variantQuantities.getOrDefault(s.getStockItemId(), 0);
                    if (vqty > 0) total += s.getEffectivePrice() * vqty;
                }
            }
        }
        totalLabel.setText("TOTAL : " + String.format("%.2f TL", total));
    }

    private void rebuildSuggestion() {
        suggestionBox.getChildren().clear();
        Label suggTitle = new Label("Optimized Suggestion:");
        suggTitle.setFont(Font.font("System", FontWeight.BOLD, 13));
        suggTitle.setTextFill(Color.web("#1b5e20"));
        suggestionBox.getChildren().add(suggTitle);

        Map<String, String> assignment = new LinkedHashMap<>();
        double optTotal = 0;

        for (Product product : basket) {
            String pId  = product.getProductId();
            int    qty  = quantities.getOrDefault(pId, 1);
            String cheapest = getCheapestChainForProduct(pId);
            assignment.put(pId, cheapest);
            if (!cheapest.isEmpty()) {
                StockItem stock = findByChain(normalize(cheapest), pId);
                if (stock != null) optTotal += stock.getEffectivePrice() * qty;
            }
        }

        Map<String, List<String>> chainToProducts = new LinkedHashMap<>();
        for (Map.Entry<String, String> e : assignment.entrySet()) {
            String chain = e.getValue().isEmpty() ? "?" : e.getValue();
            chainToProducts.computeIfAbsent(chain, k -> new ArrayList<>()).add(e.getKey());
        }

        for (Map.Entry<String, List<String>> e : chainToProducts.entrySet()) {
            StringBuilder sb = new StringBuilder(e.getKey()).append(": ");
            for (String pid : e.getValue()) {
                String pname = basket.stream().filter(p -> p.getProductId().equals(pid))
                    .map(Product::getName).findFirst().orElse(pid);
                sb.append(pname).append(", ");
            }
            if (sb.toString().endsWith(", ")) sb.setLength(sb.length() - 2);
            Label row = new Label("  " + sb.toString());
            row.setFont(Font.font("System", 12));
            row.setTextFill(Color.web("#2e7d32"));
            row.setWrapText(true);
            suggestionBox.getChildren().add(row);
        }

        int chainCount = (int) chainToProducts.keySet().stream().filter(k -> !k.equals("?")).count();
        Label countLbl = new Label("Minimum number of markets: " + chainCount +
            "  |  Optimized total: " + String.format("%.2f TL", optTotal));
        countLbl.setFont(Font.font("System", FontWeight.BOLD, 13));
        countLbl.setTextFill(Color.web("#0d47a1"));
        suggestionBox.getChildren().add(countLbl);
    }

    // ── Index & sorgular ──────────────────────────────────────────────
    private void buildIndex() {
        stockByChain.clear(); stockByStoreId.clear();
        for (StockItem s : stockItems) {
            String sid   = s.getStoreId();
            String chain = normalize(s.getChain());
            stockByStoreId.computeIfAbsent(sid, k -> new ArrayList<>()).add(s);
            if (!chain.isEmpty())
                stockByChain.computeIfAbsent(chain, k -> new ArrayList<>()).add(s);
        }
    }

    private List<String> getDistinctChains() {
        List<String> chains = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        for (Store store : stores) {
            String c = store.getChainName();
            if (c != null && !c.isEmpty() && seen.add(c)) chains.add(c);
        }
        return chains;
    }

    private String getCheapestChainForProduct(String productId) {
        String cheapestChain = "";
        double cheapestPrice = Double.MAX_VALUE;
        for (String chain : getDistinctChains()) {
            List<StockItem> all = findAllByChain(normalize(chain), productId);
            if (all.isEmpty()) continue;
            double price = all.get(0).getEffectivePrice();
            if (price < cheapestPrice) { cheapestPrice = price; cheapestChain = chain; }
        }
        return cheapestChain;
    }

    private StockItem findByChain(String chain, String productId) {
        List<StockItem> list = stockByChain.get(chain);
        if (list == null) return null;
        for (StockItem s : list)
            if (s.getProductId() != null && s.getProductId().equalsIgnoreCase(productId)) return s;
        return null;
    }

    private List<StockItem> findAllByChain(String chain, String productId) {
        List<StockItem> list = stockByChain.get(chain);
        if (list == null) return new ArrayList<>();
        return list.stream()
            .filter(s -> s.getProductId() != null && s.getProductId().equalsIgnoreCase(productId))
            .sorted(Comparator.comparingDouble(StockItem::getEffectivePrice))
            .collect(Collectors.toList());
    }

    private String normalize(String raw) {
        if (raw == null || raw.isBlank()) return "";
        return raw.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    private <T> List<T> nvl(List<T> l) { return l != null ? l : new ArrayList<>(); }

    private void setBackground(StackPane root) {
        try {
            Image bg = new Image(getClass().getResource("/data/shopping-cart.png").toExternalForm());
            root.setBackground(new Background(new BackgroundImage(bg,
                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(100, 100, true, true, true, true))));
        } catch (Exception e) { e.printStackTrace(); }
    }
}