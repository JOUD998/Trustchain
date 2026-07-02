package com.joud.trustchain.voucher.dto;

import com.joud.trustchain.voucher.VoucherStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VerifyVoucherResponse {

    private String code;

    private boolean valid;

    private VoucherStatus status;

    private String message;

    private LocalDateTime expiresAt;



}
