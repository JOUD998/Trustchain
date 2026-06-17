package com.joud.trustchain.campaign;

import com.joud.trustchain.campaign.dto.CampaignResponse;
import com.joud.trustchain.campaign.dto.CreateCampaignRequest;
import com.joud.trustchain.campaign.dto.UpdateCampaignRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.joud.trustchain.campaign.CampaignStatus.ACTIVE;

@Service
public class CampaignService {

    private final CampaignRepository campaignRepository;


    public CampaignService(CampaignRepository campaignRepository) {
        this.campaignRepository = campaignRepository;
    }

    public List<CampaignResponse> getAllCampaigns() {

        List<Campaign> campaigns = campaignRepository.findAll();
        List<CampaignResponse> campaignResponses = new ArrayList<>();
        for (Campaign campaign : campaigns){
            CampaignResponse campaignResponse =  mapToCampaignResponse(campaign);
            campaignResponses.add(campaignResponse);
        }

        return campaignResponses;
    }

    public Campaign findCampaignEntityById(Long id) {

        return campaignRepository.findById(id).orElseThrow(() -> new RuntimeException("Campaign not found with id: " + id));

    }


    public CampaignResponse createCampaign(CreateCampaignRequest request)
    {

        Campaign campaign = Campaign.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .targetAmount(request.getTargetAmount())
                .currentAmount(BigDecimal.valueOf(0))
                .status(ACTIVE)
                .build();

        campaign = campaignRepository.save(campaign);
        return mapToCampaignResponse(campaign);

    }

    public CampaignResponse getCampaignById(Long id) {
        return mapToCampaignResponse(findCampaignEntityById(id));
    }

    public CampaignResponse updateCampaign(Long id, UpdateCampaignRequest request){

        Campaign campaign = findCampaignEntityById(id);

        if (request.getTitle() != null){
            if(!request.getTitle().isBlank()){
                campaign.setTitle(request.getTitle());
            }
            else {
                throw new RuntimeException("Title must not be Blank");
            }
        }
        if (request.getDescription() != null){
            campaign.setDescription(request.getDescription());
        }
        if (request.getTargetAmount() != null)
        {
            if (request.getTargetAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("Target Amount must be Positive");
            }
            campaign.setTargetAmount(request.getTargetAmount());

        }
        if (request.getStatus() != null){
            campaign.setStatus(request.getStatus());
        }

        campaign = campaignRepository.save(campaign);
        return mapToCampaignResponse(campaign);
    }

    private CampaignResponse mapToCampaignResponse(Campaign campaign) {
        CampaignResponse campaignResponse = new CampaignResponse();
        campaignResponse.setId(campaign.getId());
        campaignResponse.setTitle(campaign.getTitle());
        campaignResponse.setDescription(campaign.getDescription());
        campaignResponse.setCurrentAmount(campaign.getCurrentAmount());
        campaignResponse.setTargetAmount(campaign.getTargetAmount());
        campaignResponse.setStatus(campaign.getStatus());
        return campaignResponse;

    }

    public void deleteCampaign(Long id) {
        findCampaignEntityById(id);
        campaignRepository.deleteById(id);
    }


}
