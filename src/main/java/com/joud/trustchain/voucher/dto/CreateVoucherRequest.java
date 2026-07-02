package com.joud.trustchain.voucher.dto;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class CreateVoucherRequest {

    @NotNull
    private Long campaignId;

    @NotNull
    private Long beneficiaryId;

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotNull
    @Future
    private LocalDateTime expiresAt;


}
