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

import model.Product;
import model.StockItem;
import model.Store;
import model.Campaign;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class BasketScreen {

    private Stage stage;
    private Scene previousScene;

    private List<Product> basket;
    private List<StockItem> stockItems;
    private List<Store> stores;
    private List<Campaign> campaigns;

    private Map<String, Integer> quantities = new LinkedHashMap<>();
    private Map<String, String> selectedChain = new LinkedHashMap<>();

    private Map<String, List<StockItem>> stockByChain   = new HashMap<>();
    private Map<String, List<StockItem>> stockByStoreId = new HashMap<>();

    private VBox itemsContainer;
    private Label totalLabel;
    private VBox suggestionBox;

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

    public void setPreviousScene(Scene scene) {
        this.previousScene = scene;
    }

    public void start(Stage stage) {
        this.stage = stage;
        buildIndex();

        StackPane root = new StackPane();
        setBackground(root);

        VBox page = new VBox(16);
        page.setPadding(new Insets(24));
        page.setAlignment(Pos.TOP_CENTER);
        page.setStyle(
            "-fx-background-color: rgba(255,255,255,0.97);" +
            "-fx-background-radius: 20;"
        );

        Label title = new Label("MY BASKET");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));
        title.setTextFill(Color.BLACK);
        page.getChildren().add(title);

        itemsContainer = new VBox(10);
        itemsContainer.setFillWidth(true);
        rebuildItems();

        ScrollPane itemsScroll = new ScrollPane(itemsContainer);
        itemsScroll.setFitToWidth(true);
        itemsScroll.setPrefHeight(380);
        itemsScroll.setStyle(
            "-fx-background: transparent;" +
            "-fx-background-color: transparent;" +
            "-fx-border-color: transparent;"
        );

        page.getChildren().add(itemsScroll);

        suggestionBox = new VBox(6);
        suggestionBox.setPadding(new Insets(14));
        suggestionBox.setStyle(
            "-fx-background-color: #e8f5e9;" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: #a5d6a7;" +
            "-fx-border-radius: 10;" +
            "-fx-border-width: 1.5;"
        );
        rebuildSuggestion();
        page.getChildren().add(suggestionBox);

        totalLabel = new Label();
        totalLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        totalLabel.setTextFill(Color.web("#0d47a1"));
        rebuildTotal();
        page.getChildren().add(totalLabel);

        Button backBtn = new Button("< BACK TO SEARCH");
        backBtn.setMaxWidth(Double.MAX_VALUE);
        backBtn.setPrefHeight(44);
        backBtn.setStyle(
            "-fx-background-color: #c62828;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 8;"
        );
        backBtn.setOnAction(e -> {
            if (previousScene != null) {
                stage.setScene(previousScene);
                stage.show();
            } else {
                stage.close();
            }
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
            String pId   = product.getProductId();
            String pName = product.getName();
            int qty = quantities.getOrDefault(pId, 1);
            String curChain = selectedChain.getOrDefault(pId, "");

            List<String> chainNames = getDistinctChains();

            VBox card = new VBox(8);
            card.setPadding(new Insets(12));
            card.setStyle(
                "-fx-background-color: white;" +
                "-fx-border-color: #cccccc;" +
                "-fx-border-width: 1.5;" +
                "-fx-background-radius: 10;" +
                "-fx-border-radius: 10;"
            );

            HBox topRow = new HBox(12);
            topRow.setAlignment(Pos.CENTER_LEFT);

            Label nameLbl = new Label(pName);
            nameLbl.setFont(Font.font("System", FontWeight.BOLD, 15));
            nameLbl.setTextFill(Color.BLACK);
            nameLbl.setMinWidth(200);

            Button minusBtn = new Button("-");
            minusBtn.setPrefWidth(32);
            minusBtn.setPrefHeight(32);
            minusBtn.setStyle("-fx-background-color: #ef9a9a; -fx-font-weight: bold; -fx-background-radius: 6;");

            Label qtyLbl = new Label(String.valueOf(qty));
            qtyLbl.setFont(Font.font("System", FontWeight.BOLD, 15));
            qtyLbl.setTextFill(Color.BLACK);
            qtyLbl.setMinWidth(28);
            qtyLbl.setAlignment(Pos.CENTER);

            Button plusBtn = new Button("+");
            plusBtn.setPrefWidth(32);
            plusBtn.setPrefHeight(32);
            plusBtn.setStyle("-fx-background-color: #a5d6a7; -fx-font-weight: bold; -fx-background-radius: 6;");

            minusBtn.setOnAction(e -> {
                int cur = quantities.getOrDefault(pId, 1);
                if (cur > 1) {
                    quantities.put(pId, cur - 1);
                    rebuildItems(); rebuildTotal(); rebuildSuggestion();
                }
            });
            plusBtn.setOnAction(e -> {
                quantities.put(pId, quantities.getOrDefault(pId, 1) + 1);
                rebuildItems(); rebuildTotal(); rebuildSuggestion();
            });

            topRow.getChildren().addAll(nameLbl, minusBtn, qtyLbl, plusBtn);
            card.getChildren().add(topRow);

            HBox chainRow = new HBox(8);
            chainRow.setAlignment(Pos.CENTER_LEFT);
            Label chainLbl = new Label("Mağaza:");
            chainLbl.setFont(Font.font("System", 12));
            chainLbl.setTextFill(Color.GRAY);
            chainRow.getChildren().add(chainLbl);

            String cheapestChain = getCheapestChainForProduct(pId);

            for (String chain : chainNames) {
                StockItem stock = findByChain(normalize(chain), pId);
                if (stock == null) continue;

                double price = stock.getEffectivePrice();
                boolean isSelected = chain.equals(curChain);
                boolean isCheapest = chain.equals(cheapestChain);

                String btnStyle;
                if (isSelected) {
                    btnStyle = "-fx-background-color: #1565c0; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-border-color: #0d47a1; -fx-border-radius: 6; -fx-border-width: 2;";
                } else if (isCheapest) {
                    btnStyle = "-fx-background-color: #c8e6c9; -fx-text-fill: #1b5e20; -fx-font-weight: bold; -fx-background-radius: 6; -fx-border-color: #388e3c; -fx-border-radius: 6; -fx-border-width: 2;";
                } else {
                    btnStyle = "-fx-background-color: #f5f5f5; -fx-text-fill: #333333; -fx-background-radius: 6; -fx-border-color: #bbbbbb; -fx-border-radius: 6; -fx-border-width: 1;";
                }

                Button chainBtn = new Button(chain + "\n" + String.format("%.2f TL", price));
                chainBtn.setPrefWidth(110);
                chainBtn.setPrefHeight(44);
                chainBtn.setStyle(btnStyle);
                chainBtn.setWrapText(true);
                chainBtn.setOnAction(e -> {
                    selectedChain.put(pId, chain);
                    rebuildItems(); rebuildTotal();
                });
                chainRow.getChildren().add(chainBtn);
            }

            card.getChildren().add(chainRow);

            if (!curChain.isEmpty()) {
                StockItem stock = findByChain(normalize(curChain), pId);
                if (stock != null) {
                    double price = stock.getEffectivePrice();
                    double lineTotal = price * qty;
                    Label lineLabel = new Label(
                        String.format("%d x %.2f TL = %.2f TL  (%s)", qty, price, lineTotal, curChain)
                    );
                    lineLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
                    lineLabel.setTextFill(Color.web("#1565c0"));
                    card.getChildren().add(lineLabel);
                }
            } else {
                Label hintLbl = new Label("Yukaridan bir magaza secin");
                hintLbl.setFont(Font.font("System", 12));
                hintLbl.setTextFill(Color.GRAY);
                card.getChildren().add(hintLbl);
            }

            itemsContainer.getChildren().add(card);
        }
    }

    private void rebuildTotal() {
        double total = 0;
        for (Product product : basket) {
            String pId  = product.getProductId();
            String chain = selectedChain.getOrDefault(pId, "");
            int qty = quantities.getOrDefault(pId, 1);
            if (chain.isEmpty()) continue;
            StockItem stock = findByChain(normalize(chain), pId);
            if (stock != null) total += stock.getEffectivePrice() * qty;
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
            String pId = product.getProductId();
            int qty = quantities.getOrDefault(pId, 1);
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
            String chain = e.getKey();
            List<String> pids = e.getValue();
            StringBuilder sb = new StringBuilder(chain).append(": ");
            for (String pid : pids) {
                String pname = basket.stream()
                    .filter(p -> p.getProductId().equals(pid))
                    .map(Product::getName)
                    .findFirst().orElse(pid);
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

    private void buildIndex() {
        stockByChain.clear();
        stockByStoreId.clear();
        for (StockItem s : stockItems) {
            String sid   = s.getStoreId();
            String chain = normalize(s.getChain());
            stockByStoreId.computeIfAbsent(sid,   k -> new ArrayList<>()).add(s);
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
            StockItem stock = findByChain(normalize(chain), productId);
            if (stock == null) continue;
            double price = stock.getEffectivePrice();
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

    private String normalize(String raw) {
        if (raw == null || raw.isBlank()) return "";
        return raw.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    private <T> List<T> nvl(List<T> l) { return l != null ? l : new ArrayList<>(); }

    private void setBackground(StackPane root) {
        try {
            File f = new File("src/data/3.jpg");
            if (f.exists()) {
                Image bg = new Image(f.toURI().toString());
                BackgroundSize size = new BackgroundSize(100, 100, true, true, true, true);
                root.setBackground(new Background(new BackgroundImage(
                    bg, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER, size)));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}