package com.joud.trustchain.payout.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreatePayoutRequest {

    @NotNull
    private Long milestoneId;

}