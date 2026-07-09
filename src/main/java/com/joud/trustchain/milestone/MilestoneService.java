package com.joud.trustchain.milestone;
import com.joud.trustchain.blockchain_transaction.BlockchainEntityType;
import com.joud.trustchain.blockchain_transaction.BlockchainTransactionService;
import com.joud.trustchain.blockchain_transaction.BlockchainTransactionType;
import com.joud.trustchain.campaign.CampaignService;
import com.joud.trustchain.milestone.dto.CreateMilestoneRequest;
import com.joud.trustchain.milestone.dto.MilestoneResponse;
import com.joud.trustchain.security.CurrentUserService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;



@Service
public class MilestoneService {

    private final MilestoneRepository milestoneRepository;
    private final CampaignService campaignService;
    private final BlockchainTransactionService blockchainTransactionService;
    private final CurrentUserService currentUserService;

    public MilestoneService(MilestoneRepository milestoneRepository, CampaignService campaignService, BlockchainTransactionService blockchainTransactionService, CurrentUserService currentUserService) {
        this.milestoneRepository = milestoneRepository;
        this.campaignService = campaignService;
        this.blockchainTransactionService = blockchainTransactionService;
        this.currentUserService = currentUserService;
    }

    public List<MilestoneResponse> getAllMilestones(){

        List<Milestone> milestones = milestoneRepository.findAll();
        List<MilestoneResponse> milestoneResponses = new ArrayList<>();

        for (Milestone milestone: milestones){
            MilestoneResponse milestoneResponse = mapToMilestoneResponse(milestone);
            milestoneResponses.add(milestoneResponse);
        }

        return milestoneResponses;

    }

    @Transactional
    public MilestoneResponse createMilestone(CreateMilestoneRequest request){

        Milestone milestone = Milestone.builder()
                .campaign(campaignService.findCampaignEntityById(request.getCampaignId()))
                .title(request.getTitle())
                .description(request.getDescription())
                .amount(request.getAmount())
                .status(MilestoneStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        milestone = milestoneRepository.save(milestone);
        blockchainTransactionService.recordTransaction(
                BlockchainTransactionType.MILESTONE_CREATED,
                BlockchainEntityType.MILESTONE,
                milestone.getId(),
                "Milestone '" + milestone.getTitle()
                        + "' was created for campaign " + milestone.getCampaign().getId()
                        + " with amount " + milestone.getAmount(),
                currentUserService.getCurrentUser().getId()

                );
        return mapToMilestoneResponse(milestone);
    }


    public List<MilestoneResponse> getMilestonesByCampaignId(Long campaignId){

       List<Milestone> milestones =  milestoneRepository.findAllByCampaign_Id(campaignId);
        List<MilestoneResponse> milestoneResponses = new ArrayList<>();
       for (Milestone milestone:milestones){
           MilestoneResponse milestoneResponse = mapToMilestoneResponse(milestone);
           milestoneResponses.add(milestoneResponse);
       }

    return milestoneResponses;

    }




    private MilestoneResponse mapToMilestoneResponse(Milestone milestone){

        MilestoneResponse milestoneResponse = new MilestoneResponse();

            milestoneResponse.setId(milestone.getId());
            milestoneResponse.setCampaignId(milestone.getCampaign().getId());
            milestoneResponse.setCampaignTitle(milestone.getCampaign().getTitle());
            milestoneResponse.setTitle(milestone.getTitle());
            milestoneResponse.setDescription(milestone.getDescription());
            milestoneResponse.setAmount(milestone.getAmount());
            milestoneResponse.setStatus(milestone.getStatus());
            milestoneResponse.setCreatedAt(milestone.getCreatedAt());
            milestoneResponse.setUpdatedAt(milestone.getUpdatedAt());

        return milestoneResponse;

    }








}
