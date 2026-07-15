package com.joud.trustchain.milestone_vote;

import com.joud.trustchain.blockchain_transaction.BlockchainEntityType;
import com.joud.trustchain.blockchain_transaction.BlockchainTransactionService;
import com.joud.trustchain.blockchain_transaction.BlockchainTransactionType;
import com.joud.trustchain.milestone.Milestone;
import com.joud.trustchain.milestone.MilestoneService;
import com.joud.trustchain.milestone.MilestoneStatus;
import com.joud.trustchain.milestone_vote.dto.CreateMilestoneVoteRequest;
import com.joud.trustchain.milestone_vote.dto.MilestoneVoteResponse;
import com.joud.trustchain.security.CurrentUserService;
import com.joud.trustchain.user.User;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class MilestoneVoteService {

    private final MilestoneVoteRepository milestoneVoteRepository;
    private final MilestoneService milestoneService;
    private final CurrentUserService currentUserService;
    private final BlockchainTransactionService blockchainTransactionService;

    public MilestoneVoteService(
            MilestoneVoteRepository milestoneVoteRepository,
            MilestoneService milestoneService,
            CurrentUserService currentUserService,
            BlockchainTransactionService blockchainTransactionService
    ) {
        this.milestoneVoteRepository = milestoneVoteRepository;
        this.milestoneService = milestoneService;
        this.currentUserService = currentUserService;
        this.blockchainTransactionService = blockchainTransactionService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'VALIDATOR')")
    @Transactional
    public MilestoneVoteResponse createVote(Long milestoneId, CreateMilestoneVoteRequest request) {

        User currentUser = currentUserService.getCurrentUser();

        if (milestoneVoteRepository.existsByMilestone_IdAndVoter_Id(milestoneId, currentUser.getId())) {
            throw new RuntimeException("You have already voted for this milestone");
        }

        Milestone milestone = milestoneService.findMilestoneEntityById(milestoneId);

        MilestoneVote vote = MilestoneVote.builder()
                .milestone(milestone)
                .voter(currentUser)
                .decision(request.getDecision())
                .comment(request.getComment())
                .createdAt(LocalDateTime.now())
                .build();

        vote = milestoneVoteRepository.save(vote);

        blockchainTransactionService.recordTransaction(
                BlockchainTransactionType.MILESTONE_VOTE_CAST,
                BlockchainEntityType.MILESTONE_VOTE,
                vote.getId(),
                "User " + currentUser.getId()
                        + " voted " + vote.getDecision()
                        + " on milestone " + milestone.getId(),
                currentUser.getId()
        );
        approveMilestoneIfEnoughVotes(milestone, currentUser.getId());
        return mapToMilestoneVoteResponse(vote);
    }

    public List<MilestoneVoteResponse> getVotesByMilestoneId(Long milestoneId) {

        List<MilestoneVote> votes = milestoneVoteRepository.findAllByMilestone_Id(milestoneId);
        List<MilestoneVoteResponse> responses = new ArrayList<>();

        for (MilestoneVote vote : votes) {
            responses.add(mapToMilestoneVoteResponse(vote));
        }

        return responses;
    }

    private void approveMilestoneIfEnoughVotes(Milestone milestone, Long currentUserId) {

        if (milestone.getStatus() == MilestoneStatus.APPROVED) {
            return;
        }

        long approveVotes = milestoneVoteRepository.countByMilestone_IdAndDecision(
                milestone.getId(),
                MilestoneVoteDecision.APPROVE
        );

        if (approveVotes >= 2) {
            milestone.setStatus(MilestoneStatus.APPROVED);
            milestone.setUpdatedAt(LocalDateTime.now());

            blockchainTransactionService.recordTransaction(
                    BlockchainTransactionType.MILESTONE_APPROVED,
                    BlockchainEntityType.MILESTONE,
                    milestone.getId(),
                    "Milestone '" + milestone.getTitle()
                            + "' was approved after " + approveVotes + " approve votes",
                    currentUserId
            );
        }
    }

    private MilestoneVoteResponse mapToMilestoneVoteResponse(MilestoneVote vote) {

        MilestoneVoteResponse response = new MilestoneVoteResponse();

        response.setId(vote.getId());
        response.setMilestoneId(vote.getMilestone().getId());
        response.setMilestoneTitle(vote.getMilestone().getTitle());
        response.setVoterId(vote.getVoter().getId());
        response.setVoterFullName(vote.getVoter().getFullName());
        response.setDecision(vote.getDecision());
        response.setComment(vote.getComment());
        response.setCreatedAt(vote.getCreatedAt());

        return response;
    }
}

