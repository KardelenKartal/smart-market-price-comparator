package ui;

import javafx.application.Application;
import javafx.stage.Stage;

import service.DataLoader;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        DataLoader loader = new DataLoader();
        DataLoader.AppData data = loader.loadAll("W1");

        SearchScreen searchScreen = new SearchScreen(
            data.products,
            data.categories,
            data.stockItems
        );
        searchScreen.setAllStores(data.stores);
        searchScreen.setAllCampaigns(data.campaigns);
        searchScreen.setAllPriceHistories(data.priceHistory);
        searchScreen.setAllStoreLocations(data.storeLocations); // YENİ
        searchScreen.setAllStartPoints(data.startPoints);       // YENİ
        searchScreen.start(stage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}