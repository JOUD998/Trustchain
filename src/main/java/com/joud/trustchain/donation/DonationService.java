package com.joud.trustchain.donation;
import com.joud.trustchain.campaign.Campaign;
import com.joud.trustchain.campaign.CampaignRepository;
import com.joud.trustchain.campaign.CampaignStatus;
import com.joud.trustchain.donation.dto.DonationRequest;
import com.joud.trustchain.donation.dto.DonationResponse;
import com.joud.trustchain.security.CurrentUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class DonationService {

    private final CurrentUserService currentUserService;
    private final DonationRepository donationRepository;
    private final CampaignRepository campaignRepository;

    public DonationService(CurrentUserService currentUserService, DonationRepository donationRepository, CampaignRepository campaignRepository){
         this.currentUserService = currentUserService;
         this.donationRepository = donationRepository;
         this.campaignRepository = campaignRepository;
    }

    public



    @Transactional
    public DonationResponse createDonation(DonationRequest request) {

        Campaign campaign = campaignRepository.findById(request.getCampaignId())
                .orElseThrow(() -> new RuntimeException("Campaign not found"));

        if (campaign.getStatus() == CampaignStatus.COMPLETED)
            throw new RuntimeException("You can't Donate, Campaign is Completed");
        if (campaign.getStatus() == CampaignStatus.CANCELLED)
            throw new RuntimeException("You can't Donate, Campaign is Cancelled");

        campaign.setCurrentAmount(campaign.getCurrentAmount().add(request.getAmount()));

        Donation donation = Donation.builder()
                .amount(request.getAmount())
                .createdAt(LocalDateTime.now())
                .campaign(campaign)
                .user(currentUserService.getCurrentUser())
                .build();

        return mapToDonationResponse(donationRepository.save(donation));
    }





    private DonationResponse mapToDonationResponse(Donation donation) {
        DonationResponse donationResponse = new DonationResponse();

        donationResponse.setId(donation.getId());
        donationResponse.setAmount(donation.getAmount());
        donationResponse.setCreatedAt(donation.getCreatedAt());

        donationResponse.setCampaignId(donation.getCampaign().getId());
        donationResponse.setCampaignTitle(donation.getCampaign().getTitle());

        donationResponse.setUserId(donation.getUser().getId());
        donationResponse.setUserFullName(donation.getUser().getFullName());

        return donationResponse;

    }

}
