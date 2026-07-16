package com.joud.trustchain.campaign.dto;

import com.joud.trustchain.campaign.CampaignStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CampaignResponse {


    private Long id;

    private String title;

    private String description;

    private BigDecimal targetAmount;

    private BigDecimal currentAmount;

    private CampaignStatus status;

    private Long organizationId;

    private String organizationName;

}
