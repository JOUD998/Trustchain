package com.joud.trustchain.milestone.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class CreateMilestoneRequest {
    @NotNull
    private Long campaignId;

    @NotBlank
    private String title;

    private String description;

    @NotNull
    @Positive
    private BigDecimal amount;






}
