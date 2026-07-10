package com.joud.trustchain.payout.dto;

import com.joud.trustchain.payout.PayoutStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PayoutResponse {

    private Long id;

    private Long milestoneId;
    private String milestoneTitle;

    private Long campaignId;
    private String campaignTitle;

    private BigDecimal amount;

    private PayoutStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime executedAt;

    private Long createdBy;
}