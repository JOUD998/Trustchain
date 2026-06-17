package com.joud.trustchain.campaign;
import com.joud.trustchain.campaign.dto.CampaignResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CampaignRepository extends JpaRepository<Campaign,Long> {

}
