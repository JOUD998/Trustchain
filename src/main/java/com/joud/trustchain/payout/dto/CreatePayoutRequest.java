package com.joud.trustchain.payout.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class CreatePayoutRequest {

    @NotNull
    private Long milestoneId;

    @NotNull
    @Positive
    private BigDecimal amount;
}