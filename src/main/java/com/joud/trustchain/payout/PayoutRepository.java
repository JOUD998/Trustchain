package com.joud.trustchain.payout;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PayoutRepository extends JpaRepository<Payout, Long> {

    boolean existsByMilestone_Id(Long milestoneId);

    List<Payout> findAllByMilestone_Id(Long milestoneId);

    List<Payout> findAllByMilestone_Campaign_Id(Long campaignId);
}