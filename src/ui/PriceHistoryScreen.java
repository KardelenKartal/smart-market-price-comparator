package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
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
import model.PriceHistory;

import java.util.*;
import java.util.stream.Collectors;

public class PriceHistoryScreen {

    private Stage stage;
    private Scene previousScene;

    private List<Product>      basket;
    private List<StockItem>    stockItems;
    private List<Store>        stores;
    private List<PriceHistory> priceHistories;
    private List<StockItem>    selectedStockItems;

    private Map<String, List<PriceHistory>> phByStockId  = new HashMap<>();
    private Map<String, StockItem>          stockById    = new HashMap<>();

    private static final String[] WEEKS = {"W1", "W2", "W3", "W4"};

    private Canvas    chartCanvas;
    private VBox      statsBox;
    private MenuButton pickBtn;
    private StockItem pickedItem = null;

    public PriceHistoryScreen(List<Product> basket, List<StockItem> stockItems,
                               List<Store> stores, List<PriceHistory> priceHistories,
                               List<StockItem> selectedStockItems) {
        this.basket             = nvl(basket);
        this.stockItems         = nvl(stockItems);
        this.stores             = nvl(stores);
        this.priceHistories     = nvl(priceHistories);
        this.selectedStockItems = nvl(selectedStockItems);
    }

    public PriceHistoryScreen(List<Product> basket, List<StockItem> stockItems,
                               List<Store> stores, List<PriceHistory> priceHistories) {
        this(basket, stockItems, stores, priceHistories, new ArrayList<StockItem>());
    }

    public void setPreviousScene(Scene scene) { this.previousScene = scene; }

    public void start(Stage stage) {
        this.stage = stage;
        buildIndex();

        StackPane root = new StackPane();
        setBackground(root);

        VBox page = new VBox(14);
        page.setPadding(new Insets(20));
        page.setAlignment(Pos.TOP_CENTER);
        page.setStyle("-fx-background-color:rgba(255,255,255,0.25);");

        Label title = new Label("PRICE HISTORY — W1 to W4");
        title.setFont(Font.font("System", FontWeight.BOLD, 22));
        title.setTextFill(Color.DARKBLUE);
        page.getChildren().add(title);

        pickBtn = new MenuButton("PICK PRODUCT ▼");
        pickBtn.setStyle(
            "-fx-background-color: #26c6da;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 6;" +
            "-fx-padding: 8 20 8 20;"
        );

        List<StockItem> availableItems = selectedStockItems.stream()
            .filter(s -> phByStockId.containsKey(s.getStockItemId()))
            .collect(Collectors.toList());

        if (availableItems.isEmpty()) {
            MenuItem mi = new MenuItem("No items selected. Please go back to Basket and select variants first.");
            mi.setDisable(true);
            pickBtn.getItems().add(mi);
            pickBtn.setText("PICK PRODUCT ▼");
        } else {
            for (StockItem si : availableItems) {
                String label = si.getProductName() + " — " + si.getBrand() + " (" + si.getChain() + ")";
                MenuItem mi = new MenuItem(label);
                mi.setOnAction(e -> {
                    pickedItem = si;
                    pickBtn.setText(label + " ▼");
                    drawChart();
                });
                pickBtn.getItems().add(mi);
            }
        }

        HBox pickRow = new HBox(pickBtn);
        pickRow.setAlignment(Pos.CENTER_LEFT);
        page.getChildren().add(pickRow);

        chartCanvas = new Canvas(880, 360);
        ScrollPane chartScroll = new ScrollPane(chartCanvas);
        chartScroll.setFitToHeight(true);
        chartScroll.setPrefHeight(380);
        chartScroll.setStyle("-fx-background:transparent; -fx-background-color:transparent; -fx-border-color:#dddddd;");
        page.getChildren().add(chartScroll);

        statsBox = new VBox(6);
        statsBox.setPadding(new Insets(12));
        statsBox.setStyle("-fx-background-color:#f5f5f5; -fx-border-color:#dddddd; -fx-border-radius:8; -fx-background-radius:8;");
        page.getChildren().add(statsBox);

        Button backBtn = new Button("< BACK TO BASKET");
        backBtn.setMaxWidth(Double.MAX_VALUE); backBtn.setPrefHeight(44);
        backBtn.setStyle("-fx-background-color:#c62828; -fx-text-fill:white; -fx-font-size:14; -fx-font-weight:bold; -fx-background-radius:8;");
        backBtn.setOnMouseEntered(e -> backBtn.setStyle("-fx-background-color:#b71c1c; -fx-text-fill:white; -fx-font-weight:bold; -fx-background-radius:8;"));
        backBtn.setOnMouseExited(e -> backBtn.setStyle("-fx-background-color:#c62828; -fx-text-fill:white; -fx-font-weight:bold; -fx-background-radius:8;"));
        backBtn.setOnAction(e -> {
            if (previousScene != null) { stage.setScene(previousScene); stage.show(); }
            else stage.close();
        });
        page.getChildren().add(backBtn);

        ScrollPane outer = new ScrollPane(page);
        outer.setFitToWidth(true);
        outer.setStyle("-fx-background:rgba(255,255,255,0.80); -fx-background-color:rgba(255,255,255,0.80);");
        root.getChildren().add(outer);

        stage.setScene(new Scene(root, 1000, 750));
        stage.show();

        if (!availableItems.isEmpty()) {
            pickedItem = availableItems.get(0);
            String lbl = pickedItem.getProductName() + " — " + pickedItem.getBrand()
                + " (" + pickedItem.getChain() + ")";
            pickBtn.setText(lbl + " ▼");
            drawChart();
        } else {
            drawEmpty();
        }
    }

    private void drawChart() {
        if (pickedItem == null) { drawEmpty(); return; }

        List<PriceHistory> phs = phByStockId.get(pickedItem.getStockItemId());
        if (phs == null || phs.isEmpty()) { drawEmpty(); return; }

        double[] prices = new double[4];
        for (PriceHistory ph : phs) {
            int wi = weekIndex(ph.getDate());
            if (wi >= 0) prices[wi] = ph.getPrice();
        }

        GraphicsContext gc = chartCanvas.getGraphicsContext2D();
        double W = chartCanvas.getWidth();
        double H = chartCanvas.getHeight();
        gc.clearRect(0, 0, W, H);

        double padL = 70, padR = 30, padT = 40, padB = 50;
        double plotW = W - padL - padR;
        double plotH = H - padT - padB;

        double minP = Arrays.stream(prices).filter(v -> v > 0).min().orElse(0);
        double maxP = Arrays.stream(prices).max().orElse(1);
        double range = maxP - minP;
        if (range < 1) range = 1;
        double yMin = Math.max(0, minP - range * 0.15);
        double yMax = maxP + range * 0.15;

        // Arka plan
        gc.setFill(Color.WHITE);
        gc.fillRect(padL, padT, plotW, plotH);

        // Grid
        gc.setStroke(Color.web("#eeeeee")); gc.setLineWidth(1);
        int gridLines = 5;
        for (int i = 0; i <= gridLines; i++) {
            double y = padT + plotH - plotH * i / gridLines;
            gc.strokeLine(padL, y, padL + plotW, y);
            double val = yMin + (yMax - yMin) * i / gridLines;
            gc.setFill(Color.web("#888888"));
            gc.setFont(Font.font("System", 11));
            gc.fillText(String.format("%.0f", val), padL - 46, y + 4);
        }

        double xStep = plotW / 3.0;

        for (int i = 0; i < 4; i++) {
            double x = padL + i * xStep;
            gc.setStroke(Color.web("#dddddd")); gc.setLineWidth(1);
            gc.strokeLine(x, padT, x, padT + plotH);
            gc.setFill(Color.web("#333333"));
            gc.setFont(Font.font("System", FontWeight.BOLD, 13));
            gc.fillText(WEEKS[i], x - 10, padT + plotH + 24);
        }

        gc.setStroke(Color.web("#aaaaaa")); gc.setLineWidth(1.5);
        gc.strokeLine(padL, padT, padL, padT + plotH);
        gc.strokeLine(padL, padT + plotH, padL + plotW, padT + plotH);

        String chartTitle = pickedItem.getProductName() + " — " + pickedItem.getBrand()
            + " (" + pickedItem.getChain() + ")";
        gc.setFill(Color.web("#1565c0"));
        gc.setFont(Font.font("System", FontWeight.BOLD, 14));
        gc.fillText(chartTitle, padL, padT - 12);

        // Çizgi + noktalar
        Color lineColor = Color.web("#1565c0");
        gc.setStroke(lineColor); gc.setLineWidth(3);

        double prevX = -1, prevY = -1;
        for (int i = 0; i < 4; i++) {
            if (prices[i] == 0) continue;
            double x = padL + i * xStep;
            double y = padT + plotH - plotH * (prices[i] - yMin) / (yMax - yMin);

            if (prevX >= 0) gc.strokeLine(prevX, prevY, x, y);
            prevX = x; prevY = y;
        }

        for (int i = 0; i < 4; i++) {
            if (prices[i] == 0) continue;
            double x = padL + i * xStep;
            double y = padT + plotH - plotH * (prices[i] - yMin) / (yMax - yMin);

            
            gc.setFill(lineColor);
            gc.fillOval(x - 7, y - 7, 14, 14);
            gc.setFill(Color.WHITE);
            gc.fillOval(x - 4, y - 4, 8, 8);

          
            gc.setFill(Color.web("#333333"));
            gc.setFont(Font.font("System", FontWeight.BOLD, 12));
            gc.fillText(String.format("%.2f", prices[i]), x - 18, y - 12);
        }


        statsBox.getChildren().clear();

        double[] validPrices = Arrays.stream(prices).filter(v -> v > 0).toArray();
        if (validPrices.length == 0) return;

        double statMin  = Arrays.stream(validPrices).min().getAsDouble();
        double statMax  = Arrays.stream(validPrices).max().getAsDouble();
        double current  = prices[3] > 0 ? prices[3] : prices[2]; 

        int minWeekIdx  = -1, maxWeekIdx = -1;
        for (int i = 0; i < 4; i++) {
            if (Math.abs(prices[i] - statMin) < 0.01) minWeekIdx = i;
            if (Math.abs(prices[i] - statMax) < 0.01) maxWeekIdx = i;
        }

        Label statsTitle = new Label("Summary");
        statsTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        statsTitle.setTextFill(Color.DARKBLUE);
        statsBox.getChildren().add(statsTitle);

        HBox statsRow = new HBox(30);
        statsRow.setAlignment(Pos.CENTER_LEFT);

       
        VBox minBox = statCard("CHEAPEST", WEEKS[minWeekIdx >= 0 ? minWeekIdx : 0],
            String.format("%.2f TL", statMin), "#1b5e20", "#e8f5e9");
    
        VBox maxBox = statCard("MOST EXPENSIVE", WEEKS[maxWeekIdx >= 0 ? maxWeekIdx : 3],
            String.format("%.2f TL", statMax), "#b71c1c", "#ffebee");
  
        VBox curBox = statCard("CURRENT (W4)", "W4",
            String.format("%.2f TL", current), "#0d47a1", "#e3f2fd");

        statsRow.getChildren().addAll(minBox, maxBox, curBox);
        statsBox.getChildren().add(statsRow);
    }

    private VBox statCard(String label, String week, String value, String textColor, String bgColor) {
        VBox box = new VBox(4);
        box.setPadding(new Insets(10, 20, 10, 20));
        box.setStyle(String.format(
            "-fx-background-color:%s; -fx-background-radius:8; -fx-border-color:%s; -fx-border-radius:8; -fx-border-width:1.5;",
            bgColor, textColor));
        box.setAlignment(Pos.CENTER);

        Label lbl = new Label(label);
        lbl.setFont(Font.font("System", FontWeight.BOLD, 11));
        lbl.setTextFill(Color.web(textColor));

        Label wk = new Label(week);
        wk.setFont(Font.font("System", 11));
        wk.setTextFill(Color.web(textColor));

        Label val = new Label(value);
        val.setFont(Font.font("System", FontWeight.BOLD, 18));
        val.setTextFill(Color.web(textColor));

        box.getChildren().addAll(lbl, wk, val);
        return box;
    }


    private void drawEmpty() {
        GraphicsContext gc = chartCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, chartCanvas.getWidth(), chartCanvas.getHeight());
        gc.setFill(Color.GRAY);
        gc.setFont(Font.font("System", 15));
        gc.fillText("No items selected. Go back to Basket, select your variants, then return here.",
            50, chartCanvas.getHeight() / 2);
        statsBox.getChildren().clear();
    }

    private void buildIndex() {
        phByStockId.clear(); stockById.clear();
        for (StockItem s : stockItems)
            stockById.put(s.getStockItemId(), s);
        for (PriceHistory ph : priceHistories)
            phByStockId.computeIfAbsent(ph.getStockItemId(), k -> new ArrayList<>()).add(ph);
    }

    private int weekIndex(String date) {
        if (date == null) return -1;
        switch (date.trim().toUpperCase()) {
            case "W1": return 0; case "W2": return 1;
            case "W3": return 2; case "W4": return 3;
            default: return -1;
        }
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