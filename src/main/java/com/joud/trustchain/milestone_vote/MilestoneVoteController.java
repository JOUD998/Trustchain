package com.joud.trustchain.milestone_vote;

import com.joud.trustchain.milestone_vote.dto.CreateMilestoneVoteRequest;
import com.joud.trustchain.milestone_vote.dto.MilestoneVoteResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/milestones/{milestoneId}/votes")
public class MilestoneVoteController {

    private final MilestoneVoteService milestoneVoteService;

    public MilestoneVoteController(MilestoneVoteService milestoneVoteService) {
        this.milestoneVoteService = milestoneVoteService;
    }

    @PostMapping
    public MilestoneVoteResponse createVote(
            @PathVariable Long milestoneId,
            @Valid @RequestBody CreateMilestoneVoteRequest request
    ) {
        return milestoneVoteService.createVote(milestoneId, request);
    }

    @GetMapping
    public List<MilestoneVoteResponse> getVotesByMilestoneId(@PathVariable Long milestoneId) {
        return milestoneVoteService.getVotesByMilestoneId(milestoneId);
    }
}