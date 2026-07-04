package com.joud.trustchain.blockchain_transaction.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BlockchainVerificationResponse {

    private boolean valid;
    private String message;
    private Long transactionId;
}
