package com.joud.trustchain.milestone_vote.dto;

import com.joud.trustchain.milestone_vote.MilestoneVoteDecision;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MilestoneVoteResponse {

    private Long id;

    private Long milestoneId;
    private String milestoneTitle;

    private Long voterId;
    private String voterFullName;

    private MilestoneVoteDecision decision;

    private String comment;

    private LocalDateTime createdAt;
}