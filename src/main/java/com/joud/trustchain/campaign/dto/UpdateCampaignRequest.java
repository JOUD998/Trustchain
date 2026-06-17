package com.joud.trustchain.campaign.dto;

import com.joud.trustchain.campaign.CampaignStatus;

import java.math.BigDecimal;

public class UpdateCampaignRequest {


    private String title;

    private String description;

    private BigDecimal targetAmount;

    private CampaignStatus status;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(BigDecimal targetAmount) {
        this.targetAmount = targetAmount;
    }

    public CampaignStatus getStatus() {
        return status;
    }

    public void setStatus(CampaignStatus status) {
        this.status = status;
    }
}
