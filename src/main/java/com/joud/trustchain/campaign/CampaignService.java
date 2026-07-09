package com.joud.trustchain.campaign;
import com.joud.trustchain.blockchain_transaction.BlockchainEntityType;
import com.joud.trustchain.blockchain_transaction.BlockchainTransactionService;
import com.joud.trustchain.blockchain_transaction.BlockchainTransactionType;
import com.joud.trustchain.campaign.dto.CampaignResponse;
import com.joud.trustchain.campaign.dto.CreateCampaignRequest;
import com.joud.trustchain.campaign.dto.UpdateCampaignRequest;
import com.joud.trustchain.security.CurrentUserService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.joud.trustchain.campaign.CampaignStatus.ACTIVE;

@Service
public class CampaignService {

    private final CampaignRepository campaignRepository;
    private final BlockchainTransactionService blockchainTransactionService;
    private final CurrentUserService currentUserService;


    public CampaignService(CampaignRepository campaignRepository, BlockchainTransactionService blockchainTransactionService, CurrentUserService currentUserService) {
        this.campaignRepository = campaignRepository;
        this.blockchainTransactionService = blockchainTransactionService;
        this.currentUserService = currentUserService;
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

    @Transactional
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
        blockchainTransactionService.recordTransaction(
                BlockchainTransactionType.CAMPAIGN_CREATED,
                BlockchainEntityType.CAMPAIGN,
                campaign.getId(),
                "Campaign " + campaign.getId() + " was created",
                currentUserService.getCurrentUser().getId()

        );
        return mapToCampaignResponse(campaign);

    }

    public CampaignResponse getCampaignById(Long id) {
        return mapToCampaignResponse(findCampaignEntityById(id));
    }

    @Transactional
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
        blockchainTransactionService.recordTransaction(
                BlockchainTransactionType.CAMPAIGN_UPDATED,
                BlockchainEntityType.CAMPAIGN,
                campaign.getId(),
                "Campaign " + campaign.getId() + " was updated",
                currentUserService.getCurrentUser().getId()

        );
        return mapToCampaignResponse(campaign);
    }


    // todo better to update this method from "delete Campaign" to "cancel Campaign" for transparency
    @Transactional
    public void deleteCampaign(Long campaignId) {
        findCampaignEntityById(campaignId);
        campaignRepository.deleteById(campaignId);
        blockchainTransactionService.recordTransaction(
                BlockchainTransactionType.CAMPAIGN_DELETED,
                BlockchainEntityType.CAMPAIGN,
                campaignId,
                "Campaign " + campaignId + " was deleted",
                currentUserService.getCurrentUser().getId()

        );
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


}
