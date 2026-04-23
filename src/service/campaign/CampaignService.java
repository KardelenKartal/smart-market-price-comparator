package service.campaign;

import model.Campaign;
import service.DataLoader;

import java.util.List;

public class CampaignService {

    private final DataLoader dataLoader;

    public CampaignService() {
        this.dataLoader = new DataLoader();
    }

    public List<Campaign> getAllCampaigns() {
        List<Campaign> campaigns = dataLoader.loadCampaigns();

        if (campaigns == null) {
            throw new RuntimeException("Campaigns could not be loaded.");
        }

        return campaigns;
    }
}