package com.joud.trustchain.donation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DonationRepository extends JpaRepository<Donation,Long>{

    List<Donation> findAllByCampaign_Id(Long campaignId);

}