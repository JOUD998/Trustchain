package com.joud.trustchain.payout;

import com.joud.trustchain.blockchain_transaction.BlockchainEntityType;
import com.joud.trustchain.blockchain_transaction.BlockchainTransactionService;
import com.joud.trustchain.blockchain_transaction.BlockchainTransactionType;
import com.joud.trustchain.milestone.Milestone;
import com.joud.trustchain.milestone.MilestoneService;
import com.joud.trustchain.milestone.MilestoneStatus;
import com.joud.trustchain.payout.dto.CreatePayoutRequest;
import com.joud.trustchain.payout.dto.PayoutResponse;
import com.joud.trustchain.security.CurrentUserService;
import com.joud.trustchain.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PayoutService {

    private final PayoutRepository payoutRepository;
    private final MilestoneService milestoneService;
    private final CurrentUserService currentUserService;
    private final BlockchainTransactionService blockchainTransactionService;

    public PayoutService(
            PayoutRepository payoutRepository,
            MilestoneService milestoneService,
            CurrentUserService currentUserService,
            BlockchainTransactionService blockchainTransactionService
    ) {
        this.payoutRepository = payoutRepository;
        this.milestoneService = milestoneService;
        this.currentUserService = currentUserService;
        this.blockchainTransactionService = blockchainTransactionService;
    }

    public List<PayoutResponse> getAllPayouts() {

        List<Payout> payouts = payoutRepository.findAll();
        List<PayoutResponse> responses = new ArrayList<>();

        for (Payout payout : payouts) {
            responses.add(mapToPayoutResponse(payout));
        }

        return responses;
    }

    public List<PayoutResponse> getPayoutsByMilestoneId(Long milestoneId) {

        List<Payout> payouts = payoutRepository.findAllByMilestone_Id(milestoneId);
        List<PayoutResponse> responses = new ArrayList<>();

        for (Payout payout : payouts) {
            responses.add(mapToPayoutResponse(payout));
        }

        return responses;
    }

    @Transactional
    public PayoutResponse createPayout(CreatePayoutRequest request) {

        User currentUser = currentUserService.getCurrentUser();

        Milestone milestone = milestoneService.findMilestoneEntityById(request.getMilestoneId());

        if (milestone.getStatus() != MilestoneStatus.APPROVED) {
            throw new RuntimeException("Payout is only allowed for approved milestones");
        }

        if (payoutRepository.existsByMilestone_Id(milestone.getId())) {
            throw new RuntimeException("Payout already exists for this milestone");
        }

        Payout payout = Payout.builder()
                .milestone(milestone)
                .amount(request.getAmount())
                .status(PayoutStatus.EXECUTED)
                .createdAt(LocalDateTime.now())
                .executedAt(LocalDateTime.now())
                .createdBy(currentUser.getId())
                .build();

        payout = payoutRepository.save(payout);

        milestone.setStatus(MilestoneStatus.COMPLETED);
        milestone.setUpdatedAt(LocalDateTime.now());

        blockchainTransactionService.recordTransaction(
                BlockchainTransactionType.PAYOUT_EXECUTED,
                BlockchainEntityType.PAYOUT,
                payout.getId(),
                "Payout of " + payout.getAmount()
                        + " was executed for milestone " + milestone.getId()
                        + " of campaign " + milestone.getCampaign().getId(),
                currentUser.getId()
        );

        return mapToPayoutResponse(payout);
    }

    private PayoutResponse mapToPayoutResponse(Payout payout) {

        PayoutResponse response = new PayoutResponse();

        response.setId(payout.getId());

        response.setMilestoneId(payout.getMilestone().getId());
        response.setMilestoneTitle(payout.getMilestone().getTitle());

        response.setCampaignId(payout.getMilestone().getCampaign().getId());
        response.setCampaignTitle(payout.getMilestone().getCampaign().getTitle());

        response.setAmount(payout.getAmount());
        response.setStatus(payout.getStatus());

        response.setCreatedAt(payout.getCreatedAt());
        response.setExecutedAt(payout.getExecutedAt());

        response.setCreatedBy(payout.getCreatedBy());

        return response;
    }
}