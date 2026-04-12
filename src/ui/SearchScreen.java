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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import model.Product;
import model.Category;
import model.StockItem;
import model.Store;
import model.Campaign;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class SearchScreen {
    private Stage stage;
    private List<Product> allProducts;
    private List<Category> allCategories;
    private List<StockItem> allStockItems;
    private List<Store> allStores;
    private List<Campaign> allCampaigns;

    private static ObservableList<Product> basketModel = FXCollections.observableArrayList();
    private ObservableList<Product> resultsModel = FXCollections.observableArrayList();
    private TextField searchBar = new TextField();
    private ComboBox<String> categoryCombo = new ComboBox<>();

    public SearchScreen(List<Product> products, List<Category> categories, List<StockItem> stockItems) {
        this.allProducts = (products != null) ? products : new ArrayList<>();
        this.allCategories = (categories != null) ? categories : new ArrayList<>();
        this.allStockItems = (stockItems != null) ? stockItems : new ArrayList<>();
    }

    public void setAllStores(List<Store> stores) { this.allStores = stores; }
    public void setAllCampaigns(List<Campaign> campaigns) { this.allCampaigns = campaigns; }

    public void start(Stage stage) {
        this.stage = stage;
        StackPane root = new StackPane();

        setBackground(root);

        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setAlignment(Pos.TOP_CENTER);

        GridPane topGrid = new GridPane();
        topGrid.setHgap(15); topGrid.setVgap(10);
        topGrid.setAlignment(Pos.TOP_LEFT);
        topGrid.setPadding(new Insets(10, 0, 0, 100));

        searchBar.setPromptText("Baş harf yazın...");
        searchBar.setPrefWidth(500);
        searchBar.setOnKeyReleased(e -> filterLogic());

        if (categoryCombo.getItems().isEmpty()) {
            categoryCombo.getItems().add("All Categories");
            allCategories.forEach(c -> {
                if (c.getName() != null) categoryCombo.getItems().add(c.getName());
            });
            categoryCombo.setValue("All Categories");
        }
        categoryCombo.setOnAction(e -> filterLogic());

        gridLabel(topGrid, "SEARCH ITEM:", 0, 0);
        topGrid.add(searchBar, 1, 0);
        gridLabel(topGrid, "CATEGORY:", 0, 1);
        topGrid.add(categoryCombo, 1, 1);

        HBox centerPanel = new HBox(40);
        centerPanel.setAlignment(Pos.CENTER);
        centerPanel.getChildren().addAll(
            createBox("AVAILABLE PRODUCTS", resultsModel, true),
            createBox("MY BASKET", basketModel, false)
        );

        Button btnCalc = new Button("CALCULATE BEST PRICES");
        btnCalc.setMaxWidth(Double.MAX_VALUE);
        btnCalc.setPrefHeight(60);
        btnCalc.setStyle("-fx-background-color: #0078D7; -fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");

        btnCalc.setOnAction(e -> {
            System.out.println("--- Butona Basıldı: Kontrol Başlıyor ---");
            try {
                if (basketModel.isEmpty()) {
                    System.out.println("HATA: Sepet boş!");
                    new Alert(Alert.AlertType.WARNING, "Sepetiniz boş!").show();
                    return;
                }

                List<Product> basketList = new ArrayList<>(basketModel);
                List<StockItem> stockList = (allStockItems != null) ? allStockItems : new ArrayList<>();
                List<Store> storeList = (allStores != null) ? allStores : new ArrayList<>();
                List<Campaign> campList = (allCampaigns != null) ? allCampaigns : new ArrayList<>();

                Scene previousScene = this.stage.getScene();

                ComparisonScreen nextScreen = new ComparisonScreen(basketList, stockList, storeList);
                nextScreen.setCampaigns(campList);
                nextScreen.setPreviousScene(previousScene);

                System.out.println("ComparisonScreen başlatılıyor...");
                nextScreen.start(this.stage);

            } catch (Exception ex) {
                System.err.println("!!! KRİTİK HATA !!!");
                ex.printStackTrace();
            }
        });

        Button btnBasket = new Button("MY BASKET");
        btnBasket.setMaxWidth(Double.MAX_VALUE);
        btnBasket.setPrefHeight(48);
        btnBasket.setStyle("-fx-background-color: #f57f17; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-background-radius: 8;");
        btnBasket.setOnAction(e -> {
            if (basketModel.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Sepetiniz bos!").show();
                return;
            }
            Scene previousScene = this.stage.getScene();
            List<StockItem> stockList = (allStockItems != null) ? allStockItems : new ArrayList<>();
            List<Store> storeList = (allStores != null) ? allStores : new ArrayList<>();
            List<Campaign> campList = (allCampaigns != null) ? allCampaigns : new ArrayList<>();
            BasketScreen basketScreen = new BasketScreen(new ArrayList<>(basketModel), stockList, storeList, campList);
            basketScreen.setPreviousScene(previousScene);
            basketScreen.start(this.stage);
        });

        mainLayout.getChildren().addAll(topGrid, centerPanel, btnBasket, btnCalc);
        root.getChildren().add(mainLayout);

        resultsModel.setAll(allProducts);
        stage.setScene(new Scene(root, 1000, 750));
        stage.show();
    }

    private void filterLogic() {
        String query = searchBar.getText().trim().toLowerCase();
        String selectedCat = categoryCombo.getValue();

        List<Product> filtered = allProducts.stream().filter(p -> {
            if (p.getName() == null) return false;
            String name = p.getName().toLowerCase();

            boolean startsMatch = query.isEmpty();
            if (!query.isEmpty()) {
                for (String word : name.split("\\s+")) {
                    if (word.startsWith(query)) { startsMatch = true; break; }
                }
            }

            String pCatName = allCategories.stream()
                .filter(c -> c.getCategoryId() != null && c.getCategoryId().equals(p.getCategoryId()))
                .map(Category::getName)
                .findFirst().orElse("");

            boolean catMatch = (selectedCat == null || selectedCat.equals("All Categories")) || pCatName.equals(selectedCat);
            return startsMatch && catMatch;
        }).collect(Collectors.toList());

        resultsModel.setAll(filtered);
    }

    private VBox createBox(String title, ObservableList<Product> model, boolean isSource) {
        VBox box = new VBox(5);
        Label l = new Label(title); l.setFont(Font.font("System", FontWeight.BOLD, 14));
        ListView<Product> lv = new ListView<>(model);
        lv.setPrefSize(400, 450);
        lv.setStyle("-fx-background-color: rgba(255, 182, 193, 0.6); -fx-control-inner-background: rgba(255, 182, 193, 0.4);");
        lv.setCellFactory(p -> new ListCell<>() {
            @Override protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.getName() == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
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
        Label l = new Label(text); l.setFont(Font.font("System", FontWeight.BOLD, 14));
        g.add(l, c, r);
    }

    private void setBackground(StackPane root) {
        try {
            File f = new File("src/data/2.jpg");
            if (f.exists()) {
                Image img = new Image(f.toURI().toString());
                BackgroundImage bImg = new BackgroundImage(img, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, new BackgroundSize(100, 100, true, true, true, true));
                root.setBackground(new Background(bImg));
            }
        } catch (Exception e) {}
    }
}