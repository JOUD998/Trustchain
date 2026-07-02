package com.joud.trustchain.voucher.dto;
import com.joud.trustchain.voucher.VoucherStatus;
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
public class VoucherResponse {

    private Long id;

    private String code;

    private BigDecimal amount;

    private VoucherStatus status;

    private LocalDateTime issuedAt;

    private LocalDateTime expiresAt;

    private LocalDateTime redeemedAt;

    private Long campaignId;

    private String campaignTitle;

    private Long beneficiaryId;

    private String beneficiaryFullName;

}
