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

public class ComparisonScreen {

    private Stage stage;
    private List<Product> basket;
    private List<StockItem> stockItems;
    private List<Store> stores;
    private List<Campaign> campaigns;

    private Map<String, List<StockItem>> stockByStoreId = new HashMap<>();
    private Map<String, List<StockItem>> stockByChain   = new HashMap<>();
    private Map<String, Campaign>        campaignByProductId = new HashMap<>();

    private Scene previousScene;

    public ComparisonScreen(List<Product> basket,
                            List<StockItem> stockItems,
                            List<Store> stores) {
        this.basket     = nvl(basket);
        this.stockItems = nvl(stockItems);
        this.stores     = nvl(stores);
        this.campaigns  = new ArrayList<>();
    }

    public void setCampaigns(List<Campaign> campaigns) {
        this.campaigns = nvl(campaigns);
    }

    public void setPreviousScene(Scene scene) {
        this.previousScene = scene;
    }

    public void start(Stage stage) {
        this.stage = stage;
        buildIndex();

        StackPane root = new StackPane();
        setBackground(root);

        VBox page = new VBox(20);
        page.setPadding(new Insets(30));
        page.setAlignment(Pos.TOP_CENTER);
        page.setStyle(
            "-fx-background-color: rgba(255,255,255,0.97);" +
            "-fx-background-radius: 20;"
        );

        Label title = new Label("FINAL COMPARISON LEADERBOARD");
        title.setFont(Font.font("System", FontWeight.BOLD, 22));
        title.setTextFill(Color.BLACK);
        page.getChildren().add(title);

        HBox storeRow = new HBox(16);
        storeRow.setAlignment(Pos.TOP_LEFT);

        Map<String, Double>  storeTotals = new LinkedHashMap<>();
        Map<String, Boolean> storeHasAny = new HashMap<>();

        for (Store store : stores) {

            String sId    = store.getStoreId();
            String sName  = store.getName();
            String sChain = normalize(store.getChainName());

            VBox card = new VBox(0);
            card.setPrefWidth(280);
            card.setStyle(
                "-fx-background-color: white;" +
                "-fx-border-color: #bbbbbb;" +
                "-fx-border-width: 1.5;" +
                "-fx-background-radius: 12;" +
                "-fx-border-radius: 12;"
            );

            Label storeLbl = new Label(sName.toUpperCase());
            storeLbl.setFont(Font.font("System", FontWeight.BOLD, 13));
            storeLbl.setTextFill(Color.WHITE);
            storeLbl.setMaxWidth(Double.MAX_VALUE);
            storeLbl.setPadding(new Insets(10, 12, 10, 12));
            storeLbl.setStyle(
                "-fx-background-color: #1565c0;" +
                "-fx-background-radius: 10 10 0 0;"
            );

            VBox itemsBox = new VBox(6);
            itemsBox.setPadding(new Insets(10, 12, 10, 12));

            double  total    = 0;
            boolean anyFound = false;

            for (Product product : basket) {

                String pId   = product.getProductId();
                String pName = product.getName();

                StockItem stock = findByStoreId(sId, pId);
                if (stock == null && !sChain.isEmpty())
                    stock = findByChain(sChain, pId);

                Label nameLbl  = new Label(pName);
                nameLbl.setFont(Font.font("System", FontWeight.BOLD, 12));
                nameLbl.setTextFill(Color.BLACK);

                Label priceLbl = new Label();
                Label tagLbl   = new Label();
                Label campLbl  = new Label();

                if (stock != null) {
                    anyFound = true;

                    double base   = stock.getCurrentPrice();
                    double final_ = stock.getEffectivePrice();
                    boolean hasDisc = (final_ < base);

                    total += final_;

                    if (hasDisc) {
                        priceLbl.setText(String.format("%.2f TL  (eski: %.2f TL)", final_, base));
                        priceLbl.setTextFill(Color.web("#e65100"));
                        priceLbl.setFont(Font.font("System", FontWeight.BOLD, 12));
                    } else {
                        priceLbl.setText(String.format("%.2f TL", final_));
                        priceLbl.setTextFill(Color.web("#1565c0"));
                        priceLbl.setFont(Font.font("System", 12));
                    }

                    String tag = stock.getAvailabilityTag() != null
                        ? stock.getAvailabilityTag().name()
                        : "UNKNOWN";
                    tagLbl.setText("[" + tag + "]");
                    tagLbl.setFont(Font.font("System", FontWeight.BOLD, 11));
                    switch (tag) {
                        case "HIGH"   -> tagLbl.setTextFill(Color.web("#2e7d32"));
                        case "MEDIUM" -> tagLbl.setTextFill(Color.web("#f57f17"));
                        default       -> tagLbl.setTextFill(Color.web("#b71c1c"));
                    }

                    Campaign camp = campaignByProductId.get(pId);
                    if (camp != null) {
                        String desc = camp.getType() + " - %" + camp.getDiscountRate();
                        campLbl.setText("[KAMPANYA] " + desc);
                        campLbl.setFont(Font.font("System", 10));
                        campLbl.setTextFill(Color.web("#6a1b9a"));
                        campLbl.setWrapText(true);
                    }

                } else {
                    priceLbl.setText("N/A");
                    priceLbl.setTextFill(Color.GRAY);
                    priceLbl.setFont(Font.font("System", 12));
                }

                VBox productBox = new VBox(2);
                productBox.setPadding(new Insets(4, 6, 4, 6));
                productBox.setStyle(
                    "-fx-background-color: #f5f5f5;" +
                    "-fx-background-radius: 6;"
                );
                productBox.getChildren().addAll(nameLbl, priceLbl, tagLbl);
                if (!campLbl.getText().isEmpty()) productBox.getChildren().add(campLbl);

                itemsBox.getChildren().add(productBox);
            }

            ScrollPane itemsScroll = new ScrollPane(itemsBox);
            itemsScroll.setFitToWidth(true);
            itemsScroll.setPrefHeight(200);
            itemsScroll.setMaxHeight(200);
            itemsScroll.setStyle(
                "-fx-background: transparent;" +
                "-fx-background-color: transparent;" +
                "-fx-border-color: transparent;"
            );

            Separator sep = new Separator();
            sep.setPadding(new Insets(4, 0, 4, 0));

            Label totalLbl;
            if (anyFound) {
                totalLbl = new Label("TOTAL:  " + String.format("%.2f TL", total));
                totalLbl.setTextFill(Color.web("#0d47a1"));
            } else {
                totalLbl = new Label("TOTAL:  N/A");
                totalLbl.setTextFill(Color.GRAY);
            }
            totalLbl.setFont(Font.font("System", FontWeight.BOLD, 14));
            totalLbl.setPadding(new Insets(8, 12, 10, 12));

            card.getChildren().addAll(storeLbl, itemsScroll, sep, totalLbl);

            storeTotals.put(sName, anyFound ? total : Double.MAX_VALUE);
            storeHasAny.put(sName, anyFound);
            storeRow.getChildren().add(card);
        }

        ScrollPane storeScroll = new ScrollPane(storeRow);
        storeScroll.setFitToHeight(true);
        storeScroll.setStyle(
            "-fx-background: transparent;" +
            "-fx-background-color: transparent;" +
            "-fx-border-color: transparent;"
        );
        storeScroll.setPrefHeight(320);

        page.getChildren().add(storeScroll);

        Optional<Map.Entry<String, Double>> winnerEntry = storeTotals.entrySet().stream()
                .filter(e -> storeHasAny.getOrDefault(e.getKey(), false))
                .min(Map.Entry.comparingByValue());

        VBox winnerBox = new VBox(6);
        winnerBox.setAlignment(Pos.CENTER);
        winnerBox.setPadding(new Insets(18));
        winnerBox.setStyle("-fx-background-color: gold; -fx-background-radius: 12;");

        if (winnerEntry.isPresent()) {
            String wName  = winnerEntry.get().getKey();
            double wTotal = winnerEntry.get().getValue();
            Label w1 = new Label(">> BEST OPTION: " + wName);
            w1.setFont(Font.font("System", FontWeight.BOLD, 18));
            w1.setTextFill(Color.BLACK);
            Label w2 = new Label(String.format("%.2f TL", wTotal));
            w2.setFont(Font.font("System", FontWeight.BOLD, 15));
            w2.setTextFill(Color.BLACK);
            winnerBox.getChildren().addAll(w1, w2);
        } else {
            Label w1 = new Label(">> NO STORE HAS ALL ITEMS");
            w1.setFont(Font.font("System", FontWeight.BOLD, 16));
            w1.setTextFill(Color.BLACK);
            winnerBox.getChildren().add(w1);
        }

        page.getChildren().add(winnerBox);

        Button backBtn = new Button("< BACK TO SEARCH");
        backBtn.setPrefHeight(48);
        backBtn.setMaxWidth(Double.MAX_VALUE);
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

        ScrollPane outerScroll = new ScrollPane(page);
        outerScroll.setFitToWidth(true);
        outerScroll.setStyle(
            "-fx-background: transparent;" +
            "-fx-background-color: transparent;"
        );

        root.getChildren().add(outerScroll);
        stage.setScene(new Scene(root, 1000, 750));
        stage.show();
    }

    private void buildIndex() {
        stockByStoreId.clear();
        stockByChain.clear();
        campaignByProductId.clear();

        for (StockItem s : stockItems) {
            String sid   = s.getStoreId();
            String chain = normalize(s.getChain());
            stockByStoreId.computeIfAbsent(sid,   k -> new ArrayList<>()).add(s);
            if (!chain.isEmpty())
                stockByChain.computeIfAbsent(chain, k -> new ArrayList<>()).add(s);
        }

        for (Campaign c : campaigns) {
            String sid = c.getStockItemId();
            if (sid != null && !sid.isEmpty())
                campaignByProductId.putIfAbsent(sid, c);
        }
    }

    private StockItem findByStoreId(String storeId, String productId) {
        List<StockItem> list = stockByStoreId.get(storeId);
        if (list == null) return null;
        for (StockItem s : list)
            if (s.getProductId() != null && s.getProductId().equalsIgnoreCase(productId)) return s;
        return null;
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
                    bg,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER,
                    size)));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}