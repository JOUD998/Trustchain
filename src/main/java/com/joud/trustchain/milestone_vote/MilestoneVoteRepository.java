package com.joud.trustchain.milestone_vote;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MilestoneVoteRepository extends JpaRepository<MilestoneVote, Long> {

    boolean existsByMilestone_IdAndVoter_Id(Long milestoneId, Long voterId);

    List<MilestoneVote> findAllByMilestone_Id(Long milestoneId);

    long countByMilestone_IdAndDecision(Long milestoneId, MilestoneVoteDecision decision);


}