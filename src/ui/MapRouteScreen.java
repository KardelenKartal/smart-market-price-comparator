package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import model.*;
import service.route.RouteOptimizer;
import util.GeoUtils;

import java.io.File;
import java.util.*;

public class MapRouteScreen {

    private Stage stage;
    private Scene previousScene;

    private List<Product>       basket;
    private List<StockItem>     stockItems;
    private List<StoreLocation> storeLocations;
    private List<StoreLocation> startPoints;
    private List<Store>         stores;
    private List<model.Campaign>     allCampaigns    = new java.util.ArrayList<>();
    private List<model.PriceHistory> allPriceHistories = new java.util.ArrayList<>();

    private static final int CANVAS_W = 920;
    private static final int CANVAS_H = 430;

    private static final double MIN_LAT = 40.963;
    private static final double MAX_LAT = 40.997;
    private static final double MIN_LON = 29.132;
    private static final double MAX_LON = 29.163;

    private double zoom   = 1.0;
    private double panX   = 0.0;
    private double panY   = 0.0;
    private double dragStartX, dragStartY;

    private Canvas mapCanvas;

    private List<StoreLocation> routeStops = new ArrayList<>();
    private double startLat  = 40.9755;
    private double startLon  = 29.1498;
    private String startName = "Başlangıç";

    private Label routeInfoLabel, distanceLabel, walkingLabel, costLabel;

    public MapRouteScreen(List<Product> basket, List<StockItem> stockItems,
                          List<StoreLocation> storeLocations, List<StoreLocation> startPoints,
                          List<Store> stores) {
        this.basket         = nvl(basket);
        this.stockItems     = nvl(stockItems);
        this.storeLocations = nvl(storeLocations);
        this.startPoints    = nvl(startPoints);
        this.stores         = nvl(stores);
        if (!this.startPoints.isEmpty()) {
            StoreLocation sp = this.startPoints.get(0);
            startLat = sp.getLatitude(); startLon = sp.getLongitude();
            startName = sp.getAddress();
        }
    }

    public void setPreviousScene(Scene scene) { this.previousScene = scene; }

    public void start(Stage stage) {
        this.stage = stage;

        StackPane root = new StackPane();
        setBackground(root);

        VBox page = new VBox(14);
        page.setPadding(new Insets(24));
        page.setAlignment(Pos.TOP_CENTER);
        page.setStyle("-fx-background-color:rgba(255,255,255,0.97);-fx-background-radius:20;");

        Label title = new Label("MAP / ROUTE");
        title.setFont(Font.font("System", FontWeight.BOLD, 22));
        title.setTextFill(Color.BLACK);

        Label subTitle = new Label("SUGGESTED ROUTE");
        subTitle.setFont(Font.font("System", FontWeight.BOLD, 15));
        subTitle.setTextFill(Color.WHITE);
        subTitle.setMaxWidth(Double.MAX_VALUE);
        subTitle.setAlignment(Pos.CENTER);
        subTitle.setPadding(new Insets(8,16,8,16));
        subTitle.setStyle("-fx-background-color:#c62828;-fx-background-radius:8;");

        HBox startRow = new HBox(10);
        startRow.setAlignment(Pos.CENTER_LEFT);
        Label startLbl = new Label("Başlangıç:");
        startLbl.setFont(Font.font("System", FontWeight.BOLD, 13));
        startLbl.setMinWidth(80);
        ComboBox<String> startCombo = new ComboBox<>();
        HBox.setHgrow(startCombo, Priority.ALWAYS);
        startCombo.setMaxWidth(Double.MAX_VALUE);
        for (StoreLocation sp : startPoints) startCombo.getItems().add(sp.getAddress());
        if (!startCombo.getItems().isEmpty()) startCombo.setValue(startCombo.getItems().get(0));
        startCombo.setOnAction(e -> {
            String sel = startCombo.getValue();
            for (StoreLocation sp : startPoints) {
                if (sel != null && sel.equals(sp.getAddress())) {
                    startLat = sp.getLatitude(); startLon = sp.getLongitude();
                    startName = sp.getAddress(); break;
                }
            }
            calculateRoute(); redraw();
        });
        startRow.getChildren().addAll(startLbl, startCombo);

        mapCanvas = new Canvas(CANVAS_W, CANVAS_H);
        Pane mapPane = new Pane(mapCanvas);
        mapPane.setPrefSize(CANVAS_W, CANVAS_H);
        mapPane.setMaxSize(CANVAS_W, CANVAS_H);
        mapPane.setCursor(Cursor.HAND);
        mapPane.setStyle("-fx-border-color:#bbbbbb;-fx-border-width:1.5;" +
                         "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.15),8,0,0,2);");

        mapPane.setOnMousePressed(e -> {
            dragStartX = e.getX(); dragStartY = e.getY();
            mapPane.setCursor(Cursor.CLOSED_HAND);
        });
        mapPane.setOnMouseDragged(e -> {
            panX += e.getX()-dragStartX; panY += e.getY()-dragStartY;
            dragStartX = e.getX(); dragStartY = e.getY(); redraw();
        });
        mapPane.setOnMouseReleased(e -> mapPane.setCursor(Cursor.HAND));
        mapPane.setOnScroll(e -> { applyZoom(e.getDeltaY()>0?1.15:0.87, e.getX(), e.getY()); redraw(); });

        Button zoomIn  = makeZoomBtn("+");
        Button zoomOut = makeZoomBtn("−");
        zoomIn.setOnAction(e  -> { applyZoom(1.2, CANVAS_W/2.0, CANVAS_H/2.0); redraw(); });
        zoomOut.setOnAction(e -> { applyZoom(0.85, CANVAS_W/2.0, CANVAS_H/2.0); redraw(); });
        VBox zoomBox = new VBox(2, zoomIn, zoomOut);
        zoomBox.setLayoutX(10); zoomBox.setLayoutY(10);
        mapPane.getChildren().add(zoomBox);

        VBox infoBox = new VBox(5);
        infoBox.setPadding(new Insets(14));
        infoBox.setStyle("-fx-background-color:#e8f5e9;-fx-background-radius:10;" +
                         "-fx-border-color:#a5d6a7;-fx-border-radius:10;-fx-border-width:1.5;");
        routeInfoLabel = makeLbl(true);  distanceLabel = makeLbl(false);
        walkingLabel   = makeLbl(false); costLabel     = makeLbl(true);
        routeInfoLabel.setText("Suggested Route: hesaplanıyor...");
        distanceLabel.setText("Distance: —"); walkingLabel.setText("Walking time: —");
        costLabel.setText("Total cost: —");
        infoBox.getChildren().addAll(routeInfoLabel, distanceLabel, walkingLabel, costLabel);

        Button basketBtn = makeActionBtn("GO TO BASKET", "#f57f17");
        basketBtn.setOnAction(e -> {
            try {
                BasketScreen basketScreen = new BasketScreen(
                    new java.util.ArrayList<>(basket), stockItems, stores, allCampaigns
                );
                basketScreen.setAllPriceHistories(allPriceHistories);
                basketScreen.setPreviousScene(previousScene);
                basketScreen.start(stage);
            } catch (Exception ex) { ex.printStackTrace(); }
        });
        addHover(basketBtn, "#f57f17", "#e65100");

        Button backBtn = makeActionBtn("< BACK TO SEARCH", "#c62828");
        backBtn.setOnAction(e -> { if (previousScene != null) { stage.setScene(previousScene); stage.show(); } });
        addHover(backBtn, "#c62828", "#b71c1c");

        page.getChildren().addAll(title, subTitle, startRow, mapPane, infoBox, basketBtn, backBtn);
        root.getChildren().add(page);
        stage.setScene(new Scene(root, 1000, 750));
        stage.show();
        calculateRoute();
        redraw();
    }

    private void calculateRoute() {
        // Sepet boşsa rota hesaplama
        if (basket == null || basket.isEmpty()) {
            routeStops.clear();
            routeInfoLabel.setText("No items in basket. Add products to see your route.");
            distanceLabel.setText("Distance: — km");
            walkingLabel.setText("Walking time: — min");
            costLabel.setText("Total cost: — TL");
            return;
        }

        List<BasketItem> items = new ArrayList<>();
        for (int i = 0; i < basket.size(); i++)
            items.add(new BasketItem(i, basket.get(i).getProductId(), 1));
        RouteResult route = new RouteOptimizer().optimizeRoute(
            items, stockItems, storeLocations, startLat, startLon);
        routeStops.clear();
        if (route != null && route.getStores() != null)
            for (RouteResultStore r : route.getStores()) {
                StoreLocation l = findLoc(r.getStoreId()); if (l != null) routeStops.add(l);
            }
        // Fallback kaldırıldı — sepet varsa ama rota bulunamadıysa bilgi ver
        if (routeStops.isEmpty()) {
            routeInfoLabel.setText("Could not find stores for your basket items.");
            distanceLabel.setText("Distance: 0.00 km");
            walkingLabel.setText("Walking time: 0 min");
            costLabel.setText("Total cost: 0.00 TL");
            return;
        }
        double dist = route.getTotalDistance();
        double cost = route.getTotalCost();
        StringBuilder sb = new StringBuilder("Start");
        for (StoreLocation s : routeStops) sb.append(" → ").append(chain(s.getStoreId()));
        routeInfoLabel.setText("Suggested Route: " + sb);
        distanceLabel.setText(String.format("Distance: %.2f km", dist));
        walkingLabel.setText(String.format("Walking time: %.0f min", GeoUtils.walkingTime(dist)));
        costLabel.setText(String.format("Total cost: %.2f TL", cost));
    }

    private void redraw() {
        GraphicsContext gc = mapCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, CANVAS_W, CANVAS_H);
        gc.setFill(Color.web("#f2efe9"));
        gc.fillRect(0, 0, CANVAS_W, CANVAS_H);

        gc.save();
        gc.translate(panX, panY);
        gc.scale(zoom, zoom);

        // ── 1. ZEMIN ────────────────────────────────────────────────────────
        gc.setFill(Color.web("#f2efe9"));
        gc.fillRect(-CANVAS_W, -CANVAS_H, CANVAS_W*3, CANVAS_H*3);

        // ── 2. YEŞİL ALANLAR ────────────────────────────────────────────────
        // Kayışdağı ormanı / Yeditepe kampüsü (mağazaların güney-doğusu)
        // Koordinat bazlı: lat 40.967-40.983, lon 29.146-40.157
        fillArea(gc, Color.web("#c8dbb8"), 40.9670, 29.1455, 40.9840, 29.1580);
        // Atapark / golf sahası (kuzey batı)
        fillArea(gc, Color.web("#d0e8c0"), 40.9870, 29.1360, 40.9940, 29.1460);
        // Küçük park (orta)
        fillArea(gc, Color.web("#cce0b4"), 40.9760, 29.1330, 40.9800, 29.1390);

        // ── 3. ANA YOLLAR (sarı/turuncu — E-5 tipi) ─────────────────────────
        // Kayışdağı Cd: lat=40.9755, yatay  (canvas y≈272)
        road(gc, Color.web("#facf5a"), 16, 40.9755, 29.132, 40.9755, 29.163);
        // İstanbul Cd: lat=40.9830, yatay   (canvas y≈177)
        road(gc, Color.web("#facf5a"), 16, 40.9830, 29.132, 40.9830, 29.163);
        // Dudullu/Atatürk Cd: lon=29.1460, dikey (canvas x≈415)
        road(gc, Color.web("#facf5a"), 16, 40.963, 29.1460, 40.997, 29.1460);
        // İkinci dikey ana yol: lon=29.1500 (canvas x≈534)
        road(gc, Color.web("#facf5a"), 16, 40.963, 29.1500, 40.997, 29.1500);

        // ── 4. ORTA YOLLAR (beyaz) ───────────────────────────────────────────
        // 19 Mayıs Cd: lat=40.9700 (canvas y≈344 civarı — güney)
        road(gc, Color.web("#ffffff"), 10, 40.9700, 29.132, 40.9700, 29.163);
        // Akyazı Cd: lat=40.9900 (canvas y≈87 civarı — kuzey)
        road(gc, Color.web("#ffffff"), 10, 40.9900, 29.132, 40.9900, 29.163);
        // Rumeli Cd: lat=40.9800 (canvas y≈215)
        road(gc, Color.web("#ffffff"), 10, 40.9800, 29.132, 40.9800, 29.163);
        // Nasır Cd: lat=40.9838 (canvas y≈167)
        road(gc, Color.web("#ffffff"), 10, 40.9838, 29.132, 40.9838, 29.163);
        // Dikey: lon=29.1430 (canvas x≈326)
        road(gc, Color.web("#ffffff"), 10, 40.963, 29.1430, 40.997, 29.1430);
        // Dikey: lon=29.1390 (canvas x≈238)
        road(gc, Color.web("#ffffff"), 10, 40.963, 29.1390, 40.997, 29.1390);
        // Dikey: lon=29.1530 (canvas x≈621)
        road(gc, Color.web("#ffffff"), 10, 40.963, 29.1530, 40.997, 29.1530);

        // ── 5. YAN SOKAKLAR (ince) ───────────────────────────────────────────
        double[][] sokaklar = {
            // yatay
            {40.9680, 29.132, 40.9680, 29.163},
            {40.9715, 29.132, 40.9715, 29.163},
            {40.9730, 29.132, 40.9730, 29.163},
            {40.9748, 29.132, 40.9748, 29.163},
            {40.9762, 29.132, 40.9762, 29.163},
            {40.9778, 29.132, 40.9778, 29.163},
            {40.9810, 29.132, 40.9810, 29.163},
            {40.9835, 29.132, 40.9835, 29.163},
            {40.9862, 29.132, 40.9862, 29.163},
            {40.9920, 29.132, 40.9920, 29.163},
            {40.9950, 29.132, 40.9950, 29.163},
            // dikey
            {40.963, 29.1345, 40.997, 29.1345},
            {40.963, 29.1370, 40.997, 29.1370},
            {40.963, 29.1410, 40.997, 29.1410},
            {40.963, 29.1442, 40.997, 29.1442},
            {40.963, 29.1473, 40.997, 29.1473},
            {40.963, 29.1490, 40.997, 29.1490},
            {40.963, 29.1510, 40.997, 29.1510},
            {40.963, 29.1545, 40.997, 29.1545},
            {40.963, 29.1570, 40.997, 29.1570},
        };
        for (double[] s : sokaklar)
            road(gc, Color.web("#ffffff"), 5, s[0], s[1], s[2], s[3]);

        // ── 6. BİNA BLOKLARI ─────────────────────────────────────────────────
        gc.setFill(Color.web("#e2dbd0"));
        // Kayışdağı Cd. kuzey tarafı blokları
        block(gc, 40.9762, 29.1345, 40.9778, 29.1385);
        block(gc, 40.9762, 29.1395, 40.9778, 29.1425);
        block(gc, 40.9762, 29.1435, 40.9778, 29.1455);
        block(gc, 40.9762, 29.1510, 40.9778, 29.1525);
        block(gc, 40.9762, 29.1545, 40.9778, 29.1590);
        // Kayışdağı Cd. güney tarafı
        block(gc, 40.9730, 29.1345, 40.9748, 29.1390);
        block(gc, 40.9730, 29.1395, 40.9748, 29.1430);
        block(gc, 40.9730, 29.1510, 40.9748, 29.1525);
        block(gc, 40.9715, 29.1345, 40.9730, 29.1385);
        block(gc, 40.9715, 29.1395, 40.9730, 29.1425);
        // İstanbul Cd. kuzey
        block(gc, 40.9838, 29.1345, 40.9862, 29.1385);
        block(gc, 40.9838, 29.1395, 40.9862, 29.1425);
        block(gc, 40.9838, 29.1510, 40.9862, 29.1525);
        block(gc, 40.9862, 29.1345, 40.9900, 29.1385);
        block(gc, 40.9862, 29.1510, 40.9900, 29.1530);
        // İstanbul Cd. güney
        block(gc, 40.9800, 29.1345, 40.9830, 29.1385);
        block(gc, 40.9800, 29.1395, 40.9830, 29.1425);
        block(gc, 40.9800, 29.1510, 40.9830, 29.1530);
        // Sağ taraf blokları
        block(gc, 40.9748, 29.1540, 40.9778, 29.1590);
        block(gc, 40.9800, 29.1540, 40.9830, 29.1590);

        // ── 7. ÖZEL YERLER ───────────────────────────────────────────────────
        // Yeditepe Üniversitesi kampüsü (güneyde, ALT_KAPI civarı)
        special(gc, Color.web("#b5ccaa"), 40.9670, 29.1435, 40.9730, 29.1520, "Yeditepe\nÜniversitesi");
        // Atapark AVM (MIGROS-02 civarı, kuzey-batı)
        special(gc, Color.web("#d0c8e0"), 40.9870, 29.1360, 40.9915, 29.1430, "Atapark");
        // Küçük AVM/Market alanı (orta)
        special(gc, Color.web("#e8dff0"), 40.9758, 29.1390, 40.9780, 29.1425, "AVM");
        // Okullar
        special(gc, Color.web("#f5e8b8"), 40.9855, 29.1345, 40.9870, 29.1385, "Okul");
        special(gc, Color.web("#f5e8b8"), 40.9708, 29.1345, 40.9722, 29.1385, "Okul");
        // Cami
        special(gc, Color.web("#c8e0d8"), 40.9800, 29.1475, 40.9812, 29.1492, "Cami");

        // ── 8. YOL İSİMLERİ ──────────────────────────────────────────────────
        roadLabel(gc, "Kayışdağı Caddesi",  40.9758, 29.1335);
        roadLabel(gc, "İstanbul Caddesi",   40.9833, 29.1335);
        roadLabel(gc, "19 Mayıs Caddesi",   40.9703, 29.1335);
        roadLabel(gc, "Akyazı Cd.",         40.9903, 29.1335);
        roadLabel(gc, "Dudullu Cd.",        40.9640, 29.1462);
        roadLabel(gc, "Atatürk Cd.",        40.9640, 29.1502);
        roadLabel(gc, "Rumeli Cd.",         40.9803, 29.1335);

        // ── 9. ROTA ──────────────────────────────────────────────────────────
        drawRoute(gc);

        // ── 10. PİNLER ───────────────────────────────────────────────────────
        drawPins(gc);

        gc.restore();

        // Köşe notu (transform dışı — sabit)
        gc.setFill(Color.rgb(0,0,0,0.35));
        gc.fillRoundRect(8, CANVAS_H-26, 215, 18, 5, 5);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("System", 10));
        gc.fillText("Scroll: zoom  |  Sürükle: kaydır", 14, CANVAS_H-13);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Çizim yardımcıları
    // ─────────────────────────────────────────────────────────────────────────
    private void fillArea(GraphicsContext gc, Color color,
                          double lat1, double lon1, double lat2, double lon2) {
        double[] p1 = c(lat1,lon1), p2 = c(lat2,lon2);
        gc.setFill(color);
        gc.fillRoundRect(Math.min(p1[0],p2[0]), Math.min(p1[1],p2[1]),
                         Math.abs(p2[0]-p1[0]), Math.abs(p2[1]-p1[1]), 5/zoom, 5/zoom);
    }

    private void road(GraphicsContext gc, Color color, double w,
                      double lat1, double lon1, double lat2, double lon2) {
        double[] p1 = c(lat1,lon1), p2 = c(lat2,lon2);
        gc.setStroke(color);
        gc.setLineWidth(w/zoom);
        gc.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        gc.strokeLine(p1[0], p1[1], p2[0], p2[1]);
    }

    private void block(GraphicsContext gc, double lat1, double lon1, double lat2, double lon2) {
        double[] p1 = c(lat1,lon1), p2 = c(lat2,lon2);
        double x = Math.min(p1[0],p2[0]), y = Math.min(p1[1],p2[1]);
        double w = Math.abs(p2[0]-p1[0]), h = Math.abs(p2[1]-p1[1]);
        gc.fillRoundRect(x, y, Math.max(w,3/zoom), Math.max(h,3/zoom), 2/zoom, 2/zoom);
        gc.setStroke(Color.web("#ccc4b4"));
        gc.setLineWidth(0.5/zoom);
        gc.strokeRoundRect(x, y, Math.max(w,3/zoom), Math.max(h,3/zoom), 2/zoom, 2/zoom);
    }

    private void special(GraphicsContext gc, Color color,
                         double lat1, double lon1, double lat2, double lon2, String label) {
        double[] p1 = c(lat1,lon1), p2 = c(lat2,lon2);
        double x = Math.min(p1[0],p2[0]), y = Math.min(p1[1],p2[1]);
        double w = Math.abs(p2[0]-p1[0]), h = Math.abs(p2[1]-p1[1]);
        gc.setFill(color);
        gc.fillRoundRect(x, y, w, h, 4/zoom, 4/zoom);
        gc.setStroke(color.darker());
        gc.setLineWidth(0.8/zoom);
        gc.strokeRoundRect(x, y, w, h, 4/zoom, 4/zoom);
        if (zoom > 0.55) {
            gc.setFill(Color.web("#3a3a3a"));
            gc.setFont(Font.font("System", FontWeight.BOLD, 8/zoom));
            gc.setTextAlign(TextAlignment.CENTER);
            for (String line : label.split("\n")) {
                gc.fillText(line, x+w/2, y+h/2+3/zoom);
                y += 9/zoom;
            }
        }
    }

    private void roadLabel(GraphicsContext gc, String name, double lat, double lon) {
        if (zoom < 0.65) return;
        double[] p = c(lat, lon);
        gc.setFill(Color.web("#777777"));
        gc.setFont(Font.font("System", 7.5/zoom));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText(name, p[0], p[1]);
    }

    private void drawRoute(GraphicsContext gc) {
        if (routeStops.isEmpty()) return;
        List<double[]> pts = new ArrayList<>();
        pts.add(c(startLat, startLon));
        for (StoreLocation s : routeStops) pts.add(c(s.getLatitude(), s.getLongitude()));
        // Gölge
        gc.setStroke(Color.rgb(0,0,0,0.10));
        gc.setLineWidth(9/zoom); gc.setLineDashes(0);
        for (int i=0; i<pts.size()-1; i++)
            gc.strokeLine(pts.get(i)[0]+2/zoom, pts.get(i)[1]+2/zoom,
                          pts.get(i+1)[0]+2/zoom, pts.get(i+1)[1]+2/zoom);
        // Beyaz kenar
        gc.setStroke(Color.WHITE); gc.setLineWidth(7/zoom);
        for (int i=0; i<pts.size()-1; i++)
            gc.strokeLine(pts.get(i)[0], pts.get(i)[1], pts.get(i+1)[0], pts.get(i+1)[1]);
        // Kırmızı kesikli
        gc.setStroke(Color.web("#e53935")); gc.setLineWidth(4/zoom);
        gc.setLineDashes(12/zoom, 7/zoom);
        for (int i=0; i<pts.size()-1; i++)
            gc.strokeLine(pts.get(i)[0], pts.get(i)[1], pts.get(i+1)[0], pts.get(i+1)[1]);
        gc.setLineDashes(0); gc.setLineWidth(1);
    }

    private void drawPins(GraphicsContext gc) {
        Set<String> rids = new HashSet<>();
        for (StoreLocation s : routeStops) rids.add(s.getStoreId());
        for (StoreLocation loc : storeLocations) {
            if (rids.contains(loc.getStoreId())) continue;
            double[] p = c(loc.getLatitude(), loc.getLongitude());
            pin(gc, p[0], p[1], Color.web("#1976d2"), Color.web("#e3f2fd"), chain(loc.getStoreId()));
        }
        for (StoreLocation s : routeStops) {
            double[] p = c(s.getLatitude(), s.getLongitude());
            pin(gc, p[0], p[1], Color.web("#2e7d32"), Color.web("#e8f5e9"), chain(s.getStoreId()));
        }
        double[] sp = c(startLat, startLon);
        pin(gc, sp[0], sp[1], Color.web("#c62828"), Color.web("#ffebee"), "Sen");
    }

    private void pin(GraphicsContext gc, double x, double y,
                     Color main, Color light, String label) {
        double r = 12/zoom;
        gc.setFill(Color.rgb(0,0,0,0.18));
        gc.fillOval(x-r+1.5/zoom, y-r*2+2/zoom, r*2, r*2);
        gc.setFill(main);
        gc.fillOval(x-r, y-r*2, r*2, r*2);
        gc.setFill(light);
        gc.fillOval(x-r*0.52, y-r*1.52, r*1.04, r*1.04);
        gc.setFill(main);
        gc.fillPolygon(new double[]{x-5/zoom, x+5/zoom, x},
                       new double[]{y-r*0.85, y-r*0.85, y+5/zoom}, 3);
        gc.setFont(Font.font("System", FontWeight.BOLD, 10/zoom));
        double tw = label.length()*6/zoom + 10/zoom;
        gc.setFill(Color.rgb(255,255,255,0.95));
        gc.fillRoundRect(x+r+2/zoom, y-r*2-1/zoom, tw, 15/zoom, 4/zoom, 4/zoom);
        gc.setStroke(main); gc.setLineWidth(0.8/zoom);
        gc.strokeRoundRect(x+r+2/zoom, y-r*2-1/zoom, tw, 15/zoom, 4/zoom, 4/zoom);
        gc.setFill(main); gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText(label, x+r+5/zoom, y-r*2+10/zoom);
        gc.setLineWidth(1);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Koordinat → canvas piksel (Mercator projeksiyonu)
    // ─────────────────────────────────────────────────────────────────────────
    private double[] c(double lat, double lon) {
        double x = (lon - MIN_LON) / (MAX_LON - MIN_LON) * CANVAS_W;
        double latRad    = Math.toRadians(lat);
        double minLatRad = Math.toRadians(MIN_LAT);
        double maxLatRad = Math.toRadians(MAX_LAT);
        double mercN   = Math.log(Math.tan(Math.PI/4 + latRad/2));
        double mercMin = Math.log(Math.tan(Math.PI/4 + minLatRad/2));
        double mercMax = Math.log(Math.tan(Math.PI/4 + maxLatRad/2));
        double y = (mercMax - mercN) / (mercMax - mercMin) * CANVAS_H;
        return new double[]{x, y};
    }

    private void applyZoom(double f, double mx, double my) {
        double nz = zoom * f;
        if (nz < 0.35 || nz > 10.0) return;
        panX = mx - (mx - panX) * f;
        panY = my - (my - panY) * f;
        zoom = nz;
    }

    private StoreLocation findLoc(String id) {
        for (StoreLocation l : storeLocations) if (l.getStoreId().equalsIgnoreCase(id)) return l;
        return null;
    }

    private String chain(String id) {
        for (Store s : stores) if (s.getStoreId().equalsIgnoreCase(id)) return s.getChainName();
        if (id.startsWith("A101"))   return "A101";
        if (id.startsWith("BIM"))    return "BIM";
        if (id.startsWith("SOK"))    return "SOK";
        if (id.startsWith("MIGROS")) return "Migros";
        if (id.startsWith("CF"))     return "CarrefourSA";
        return id;
    }

    private Label makeLbl(boolean bold) {
        Label l = new Label();
        l.setFont(Font.font("System", bold?FontWeight.BOLD:FontWeight.NORMAL, 13));
        l.setTextFill(Color.BLACK); l.setWrapText(true); return l;
    }

    private Button makeZoomBtn(String t) {
        Button b = new Button(t); b.setPrefSize(32,32);
        b.setStyle("-fx-font-size:16;-fx-font-weight:bold;-fx-background-color:#fff;" +
                   "-fx-border-color:#999;-fx-border-radius:4;-fx-background-radius:4;");
        return b;
    }

    private void addHover(Button btn, String normalColor, String hoverColor) {
        String base = "-fx-text-fill:white;-fx-font-size:14;-fx-font-weight:bold;-fx-background-radius:8;";
        btn.setOnMouseEntered(e -> {
            btn.setScaleX(1.06); btn.setScaleY(1.06);
            btn.setStyle("-fx-background-color:" + hoverColor + ";" + base);
        });
        btn.setOnMouseExited(e -> {
            btn.setScaleX(1.0); btn.setScaleY(1.0);
            btn.setStyle("-fx-background-color:" + normalColor + ";" + base);
        });
    }

    private Button makeActionBtn(String t, String color) {
        Button b = new Button(t); b.setMaxWidth(Double.MAX_VALUE); b.setPrefHeight(44);
        b.setStyle("-fx-background-color:"+color+";-fx-text-fill:white;" +
                   "-fx-font-size:14;-fx-font-weight:bold;-fx-background-radius:8;");
        return b;
    }

    private void setBackground(StackPane root) {
        try {
            File f = new File("src/data/3.jpg");
            if (f.exists()) {
                Image bg = new Image(f.toURI().toString());
                root.setBackground(new Background(new BackgroundImage(bg,
                    BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER, new BackgroundSize(100,100,true,true,true,true))));
            }
        } catch (Exception ignored) {}
    }

    private <T> List<T> nvl(List<T> l) { return l != null ? l : new ArrayList<>(); }
}