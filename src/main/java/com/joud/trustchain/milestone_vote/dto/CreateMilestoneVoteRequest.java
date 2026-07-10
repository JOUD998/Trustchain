package com.joud.trustchain.milestone_vote.dto;

import com.joud.trustchain.milestone_vote.MilestoneVoteDecision;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateMilestoneVoteRequest {

    @NotNull
    private MilestoneVoteDecision decision;

    private String comment;
}