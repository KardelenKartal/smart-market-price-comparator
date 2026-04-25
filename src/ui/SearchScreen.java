package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import model.Product;
import model.Category;
import model.StockItem;
import model.Store;
import model.Campaign;
import model.PriceHistory;
import model.StoreLocation;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class SearchScreen {

    private Stage stage;

    private List<Product>       allProducts;
    private List<Category>      allCategories;
    private List<StockItem>     allStockItems;
    private List<Store>         allStores;
    private List<Campaign>      allCampaigns;
    private List<PriceHistory>  allPriceHistories;
    private List<StoreLocation> allStoreLocations;
    private List<StoreLocation> allStartPoints;

    private static ObservableList<Product>   basketModel   = FXCollections.observableArrayList();
    private ObservableList<Product>          resultsModel  = FXCollections.observableArrayList();
    // Varyant modu için ayrı liste (StockItem brand'leri)
    private ObservableList<String>           variantModel  = FXCollections.observableArrayList();

    private TextField        searchBar     = new TextField();
    private ComboBox<String> categoryCombo = new ComboBox<>();
    private ComboBox<String> productCombo  = new ComboBox<>();  // YENİ

    // Varyant modunda ürün ismi → StockItem map'i (sepete eklemek için)
    private Map<String, StockItem> brandToStock = new LinkedHashMap<>();

    // Hangi mod: false=product listesi, true=varyant listesi
    private boolean variantMode = false;

    // Listeler (ikisi de oluşturulur, biri gizlenir)
    private ListView<Product> productListView;
    private ListView<String>  variantListView;
    private VBox              leftBox;
    private Label             leftTitle;

    public SearchScreen(List<Product> products, List<Category> categories, List<StockItem> stockItems) {
        this.allProducts    = (products   != null) ? products   : new ArrayList<>();
        this.allCategories  = (categories != null) ? categories : new ArrayList<>();
        this.allStockItems  = (stockItems != null) ? stockItems : new ArrayList<>();
    }

    public void setAllStores(List<Store> stores)                   { this.allStores         = stores; }
    public void setAllCampaigns(List<Campaign> campaigns)          { this.allCampaigns      = campaigns; }
    public void setAllPriceHistories(List<PriceHistory> histories) { this.allPriceHistories = histories; }
    public void setAllStoreLocations(List<StoreLocation> locs)     { this.allStoreLocations = locs; }
    public void setAllStartPoints(List<StoreLocation> pts)         { this.allStartPoints    = pts; }

    public void start(Stage stage) {
        this.stage = stage;

        StackPane root = new StackPane();
        setBackground(root);

        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setAlignment(Pos.TOP_CENTER);

        GridPane topGrid = new GridPane();
        topGrid.setHgap(10);
        topGrid.setVgap(10);
        topGrid.setAlignment(Pos.TOP_LEFT);
        topGrid.setPadding(new Insets(10, 0, 0, 60));

        searchBar.setPromptText("Search items...");
        searchBar.setPrefWidth(500);
        searchBar.setOnKeyReleased(e -> filterLogic());

        // Kategori combo — orijinal
        if (categoryCombo.getItems().isEmpty()) {
            categoryCombo.getItems().add("All Categories");
            allCategories.forEach(c -> {
                if (c.getName() != null) categoryCombo.getItems().add(c.getName());
            });
            categoryCombo.setValue("All Categories");
        }
        categoryCombo.setOnAction(e -> onCategoryChanged());

        // Product combo — kategori seçilince dolar, başta pasif
        productCombo.getItems().add("All Products");
        productCombo.setValue("All Products");
        productCombo.setPrefWidth(180);
        productCombo.setDisable(true);
        productCombo.setOpacity(0.5);
        productCombo.setOnAction(e -> onProductChanged());

        gridLabel(topGrid, "SEARCH ITEMS:", 0, 0);
        topGrid.add(searchBar, 1, 0);
        gridLabel(topGrid, "CATEGORY:", 0, 1);
        topGrid.add(categoryCombo, 1, 1);
        gridLabel(topGrid, "PRODUCTS:", 2, 1);
        topGrid.add(productCombo, 3, 1);

        // Sol liste: ürünler veya varyantlar
        productListView = buildProductListView();
        variantListView = buildVariantListView();
        variantListView.setVisible(false);
        variantListView.setManaged(false);

        leftTitle = new Label("AVAILABLE PRODUCTS");
        leftTitle.setFont(Font.font("System", FontWeight.BOLD, 14));

        leftBox = new VBox(5);
        leftBox.setStyle(
            "-fx-background-color: rgba(255, 105, 180, 0.75);" +
            "-fx-background-radius: 15;" +
            "-fx-padding: 10;"
        );
        leftBox.getChildren().addAll(leftTitle, productListView, variantListView);

        // Sepet listesi — çift tıkla çıkar
        VBox rightBox = new VBox(5);
        rightBox.setStyle(
            "-fx-background-color: rgba(255, 105, 180, 0.75);" +
            "-fx-background-radius: 15;" +
            "-fx-padding: 10;"
        );
        Label rightTitle = new Label("MY BASKET  (double-click to remove)");
        rightTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        ListView<Product> basketListView = new ListView<>(basketModel);
        basketListView.setPrefSize(400, 450);
        basketListView.setStyle("-fx-background-color: transparent;");
        basketListView.setCellFactory(p -> new ListCell<>() {
            @Override protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        basketListView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Product sel = basketListView.getSelectionModel().getSelectedItem();
                if (sel != null) basketModel.remove(sel);
            }
        });
        rightBox.getChildren().addAll(rightTitle, basketListView);

        HBox centerPanel = new HBox(40);
        centerPanel.setAlignment(Pos.CENTER);
        centerPanel.getChildren().addAll(leftBox, rightBox);

        Button btnBasket = new Button("MY BASKET");
        btnBasket.setMaxWidth(Double.MAX_VALUE);
        btnBasket.setPrefHeight(48);
        btnBasket.setStyle("-fx-background-color:#8c755e; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
        btnBasket.setOnAction(e -> {
            if (basketModel.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Your cart is empty!").show();
                return;
            }
            Scene previousScene = this.stage.getScene();
            BasketScreen basketScreen = new BasketScreen(
                new ArrayList<>(basketModel), allStockItems, allStores, allCampaigns
            );
            basketScreen.setAllPriceHistories(allPriceHistories);
            basketScreen.setPreviousScene(previousScene);
            basketScreen.start(this.stage);
        });



        Button btnMap = new Button("MAP / ROUTE");
        btnMap.setMaxWidth(Double.MAX_VALUE);
        btnMap.setPrefHeight(48);
        btnMap.setStyle("-fx-background-color: #8c5e6e; -fx-text-fill: black; -fx-font-size: 16px; -fx-font-weight: bold;");
        btnMap.setOnAction(e -> {
            try {
                Scene previousScene = this.stage.getScene();
                MapRouteScreen mapScreen = new MapRouteScreen(
                    new ArrayList<>(basketModel), allStockItems,
                    allStoreLocations != null ? allStoreLocations : new ArrayList<>(),
                    allStartPoints    != null ? allStartPoints    : new ArrayList<>(),
                    allStores         != null ? allStores         : new ArrayList<>()
                );
                mapScreen.setPreviousScene(previousScene);
                mapScreen.start(this.stage);
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        Button btnCalc = new Button("TAP TO COMPARE");
        btnCalc.setMaxWidth(Double.MAX_VALUE);
        btnCalc.setPrefHeight(60);
        btnCalc.setStyle("-fx-background-color: #7db894; -fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");
        btnCalc.setOnAction(e -> {
            try {
                if (basketModel.isEmpty()) {
                    new Alert(Alert.AlertType.WARNING, "Your basket is empty!").show();
                    return;
                }
                Scene previousScene = this.stage.getScene();
                ComparisonScreen nextScreen = new ComparisonScreen(
                    new ArrayList<>(basketModel), allStockItems, allStores
                );
                nextScreen.setCampaigns(allCampaigns);
                nextScreen.setPreviousScene(previousScene);
                nextScreen.start(this.stage);
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        mainLayout.getChildren().addAll(
            topGrid, centerPanel, btnBasket, btnMap, btnCalc
        );

        root.getChildren().add(mainLayout);
        resultsModel.setAll(allProducts);

        stage.setScene(new Scene(root, 1000, 750));
        stage.show();
    }

    // ── Kategori değişince product combo'yu doldur ────────────────────
    private void onCategoryChanged() {
        String selectedCat = categoryCombo.getValue();

        productCombo.getItems().clear();
        productCombo.getItems().add("All Products");
        productCombo.setValue("All Products");

        if (selectedCat == null || selectedCat.equals("All Categories")) {
            productCombo.setDisable(true);
            productCombo.setOpacity(0.5);
        } else {
            // O kategorideki ürün isimlerini ekle
            allProducts.stream()
                .filter(p -> selectedCat.equalsIgnoreCase(p.getCategoryName()))
                .map(Product::getName)
                .filter(Objects::nonNull)
                .distinct().sorted()
                .forEach(productCombo.getItems()::add);
            productCombo.setDisable(false);
            productCombo.setOpacity(1.0);
        }

        // Varyant modundan çık
        switchToProductMode();
        filterLogic();
    }

    // ── Ürün seçilince varyantları göster ────────────────────────────
    private void onProductChanged() {
        String selectedProduct = productCombo.getValue();

        if (selectedProduct == null || selectedProduct.equals("All Products")) {
            switchToProductMode();
            filterLogic();
            return;
        }

        // Bu ürüne ait benzersiz brand'leri topla (her brand için en ucuz fiyatı al)
        brandToStock.clear();
        for (StockItem s : allStockItems) {
            if (!selectedProduct.equalsIgnoreCase(s.getProductName())) continue;
            String brand = s.getBrand() != null ? s.getBrand().trim() : "";
            if (brand.isEmpty()) continue;
            String bKey = brand.trim().toLowerCase(new Locale("tr","TR"))
                .replace('ş','s').replace('ı','i').replace('ö','o')
                .replace('ü','u').replace('ç','c').replace('ğ','g');
            if (!brandToStock.containsKey(bKey) ||
                s.getEffectivePrice() < brandToStock.get(bKey).getEffectivePrice()) {
                brandToStock.put(bKey, s);
            }
        }

        // Fiyata göre sırala
        List<String> sorted = brandToStock.entrySet().stream()
            .sorted(Map.Entry.comparingByValue(
                Comparator.comparingDouble(StockItem::getEffectivePrice)))
            .map(e -> e.getValue().getBrand())
            .collect(Collectors.toList());

        variantModel.setAll(sorted);
        switchToVariantMode(selectedProduct);
    }

    // ── Mod geçişleri ─────────────────────────────────────────────────
    private void switchToVariantMode(String productName) {
        variantMode = true;
        leftTitle.setText("VARIANTS: " + productName);
        productListView.setVisible(false);
        productListView.setManaged(false);
        variantListView.setVisible(true);
        variantListView.setManaged(true);
    }

    private void switchToProductMode() {
        variantMode = false;
        leftTitle.setText("AVAILABLE PRODUCTS");
        variantListView.setVisible(false);
        variantListView.setManaged(false);
        productListView.setVisible(true);
        productListView.setManaged(true);
    }

    // ── Filtre (sadece product modunda çalışır) ───────────────────────
    private void filterLogic() {
        if (variantMode) return;

        String query = searchBar.getText().trim().toLowerCase(Locale.ROOT);
        String selectedCat = categoryCombo.getValue();

        List<Product> filtered = allProducts.stream().filter(p -> {
            if (p.getName() == null) return false;
            String name = p.getName().toLowerCase(Locale.ROOT);
            boolean nameMatch = query.isEmpty();
            if (!query.isEmpty()) {
                // Türkçe normalize
                String normName  = normalize(name);
                String normQuery = normalize(query);
                nameMatch = normName.contains(normQuery);
            }
            String pCatName = allCategories.stream()
                .filter(c -> c.getCategoryId() != null && c.getCategoryId().equals(p.getCategoryId()))
                .map(Category::getName).findFirst().orElse("");
            boolean catMatch = (selectedCat == null || selectedCat.equals("All Categories"))
                || pCatName.equals(selectedCat);
            return nameMatch && catMatch;
        }).collect(Collectors.toList());

        resultsModel.setAll(filtered);
    }

    // ── Liste görünümleri ─────────────────────────────────────────────

    private ListView<Product> buildProductListView() {
        ListView<Product> lv = new ListView<>(resultsModel);
        lv.setPrefSize(400, 450);
        lv.setStyle("-fx-background-color: transparent;");
        lv.setCellFactory(p -> new ListCell<>() {
            @Override protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        lv.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Product s = lv.getSelectionModel().getSelectedItem();
                if (s != null && !basketModel.contains(s)) basketModel.add(s);
            }
        });
        return lv;
    }

    private ListView<String> buildVariantListView() {
        ListView<String> lv = new ListView<>(variantModel);
        lv.setPrefSize(400, 450);
        lv.setStyle("-fx-background-color: transparent;");
        lv.setCellFactory(p -> new ListCell<>() {
            @Override protected void updateItem(String brand, boolean empty) {
                super.updateItem(brand, empty);
                if (empty || brand == null) { setText(null); return; }
                // Fiyatı da göster
                StockItem s = brandToStock.get(normBrandKey(brand));
                if (s != null)
                    setText(String.format("%-28s  %.2f TL", brand.trim(), s.getEffectivePrice()));
                else
                    setText(brand.trim());
                setFont(Font.font("Monospaced", 12));
            }
        });
        lv.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String brand = lv.getSelectionModel().getSelectedItem();
                if (brand == null) return;
                StockItem s = brandToStock.get(normBrandKey(brand));
                if (s == null) return;
                // Sepete ürünün Product nesnesini ekle (basketModel Product listesi tutuyor)
                Product prod = allProducts.stream()
                    .filter(p -> p.getProductId().equalsIgnoreCase(s.getProductId()))
                    .findFirst().orElse(null);
                if (prod != null && !basketModel.contains(prod))
                    basketModel.add(prod);
            }
        });
        return lv;
    }

    // Sağ taraftaki sepet kutusu (orijinal createBox)
    private VBox createBox(String title, ObservableList<Product> model, boolean isSource) {
        VBox box = new VBox(5);
        box.setStyle(
            "-fx-background-color: rgba(255, 105, 180, 0.75);" +
            "-fx-background-radius: 15;" +
            "-fx-padding: 10;"
        );
        Label l = new Label(title);
        l.setFont(Font.font("System", FontWeight.BOLD, 14));

        ListView<Product> lv = new ListView<>(model);
        lv.setPrefSize(400, 450);
        lv.setStyle("-fx-background-color: transparent;");
        lv.setCellFactory(p -> new ListCell<>() {
            @Override protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        if (isSource) {
            lv.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2) {
                    Product s = lv.getSelectionModel().getSelectedItem();
                    if (s != null && !basketModel.contains(s)) basketModel.add(s);
                }
            });
        }
        box.getChildren().addAll(l, lv);
        return box;
    }

    private void gridLabel(GridPane g, String text, int c, int r) {
        Label l = new Label(text);
        l.setFont(Font.font("System", FontWeight.BOLD, 14));
        g.add(l, c, r);
    }

    // Türkçe normalize
    private static String normalize(String s) {
        if (s == null) return "";
        return s.toLowerCase(new Locale("tr","TR"))
            .replace('ş','s').replace('ı','i').replace('ö','o')
            .replace('ü','u').replace('ç','c').replace('ğ','g');
    }

    private static String normBrandKey(String brand) {
        if (brand == null) return "";
        return brand.trim().toLowerCase(new Locale("tr","TR"))
            .replace('ş','s').replace('ı','i').replace('ö','o')
            .replace('ü','u').replace('ç','c').replace('ğ','g');
    }

    private void setBackground(StackPane root) {
        try {
            File f = new File("src/data/2.jpg");
            if (f.exists()) {
                Image img = new Image(f.toURI().toString());
                root.setBackground(new Background(new BackgroundImage(
                    img, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER,
                    new BackgroundSize(100, 100, true, true, true, true)
                )));
            }
        } catch (Exception ignored) {}
    }
}