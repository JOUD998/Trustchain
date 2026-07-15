package com.joud.trustchain.donation;
import com.joud.trustchain.blockchain_transaction.BlockchainEntityType;
import com.joud.trustchain.blockchain_transaction.BlockchainTransactionService;
import com.joud.trustchain.blockchain_transaction.BlockchainTransactionType;
import com.joud.trustchain.campaign.Campaign;
import com.joud.trustchain.campaign.CampaignRepository;
import com.joud.trustchain.campaign.CampaignStatus;
import com.joud.trustchain.donation.dto.DonationRequest;
import com.joud.trustchain.donation.dto.DonationResponse;
import com.joud.trustchain.security.CurrentUserService;
import com.joud.trustchain.user.User;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class DonationService {

    private final CurrentUserService currentUserService;
    private final DonationRepository donationRepository;
    private final CampaignRepository campaignRepository;
    private final BlockchainTransactionService blockchainTransactionService;

    public DonationService(CurrentUserService currentUserService, DonationRepository donationRepository, CampaignRepository campaignRepository, BlockchainTransactionService blockchainTransactionService){
         this.currentUserService = currentUserService;
         this.donationRepository = donationRepository;
         this.campaignRepository = campaignRepository;
        this.blockchainTransactionService = blockchainTransactionService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DONOR')")
    @Transactional
    public DonationResponse createDonation(DonationRequest request) {

        User currentUser = currentUserService.getCurrentUser();

        Campaign campaign = campaignRepository.findById(request.getCampaignId())
                .orElseThrow(() -> new RuntimeException("Campaign not found"));

        if (campaign.getStatus() == CampaignStatus.COMPLETED)
            throw new RuntimeException("You can't Donate, Campaign is Completed");
        if (campaign.getStatus() == CampaignStatus.CANCELED)
            throw new RuntimeException("You can't Donate, Campaign is Canceled");

        campaign.setCurrentAmount(campaign.getCurrentAmount().add(request.getAmount()));

        Donation donation = Donation.builder()
                .amount(request.getAmount())
                .createdAt(LocalDateTime.now())
                .campaign(campaign)
                .user(currentUser)
                .build();

        donation = donationRepository.save(donation);

        blockchainTransactionService.recordTransaction(
                BlockchainTransactionType.DONATION_CREATED,
                BlockchainEntityType.DONATION,
                donation.getId(),
                "User " + donation.getUser().getId()
                        + " donated " + donation.getAmount()
                        + " to campaign " + donation.getCampaign().getId(),
                currentUser.getId()
        );


        return mapToDonationResponse(donation);
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
