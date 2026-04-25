package test;

import model.Campaign;
import org.junit.jupiter.api.Test;
import service.campaign.CampaignService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CampaignServiceTest {

    @Test
    void shouldLoadCampaignsSuccessfully() {
        CampaignService service = new CampaignService();
        List<Campaign> campaigns = service.getAllCampaigns();

        assertNotNull(campaigns);
        assertFalse(campaigns.isEmpty());
    }
}