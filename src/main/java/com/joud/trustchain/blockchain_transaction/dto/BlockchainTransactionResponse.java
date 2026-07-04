package com.joud.trustchain.blockchain_transaction.dto;
import com.joud.trustchain.blockchain_transaction.BlockchainEntityType;
import com.joud.trustchain.blockchain_transaction.BlockchainTransactionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BlockchainTransactionResponse {

    private Long id;
    private BlockchainTransactionType transactionType;
    private BlockchainEntityType entityType;
    private Long entityId;
    private String data;
    private String previousHash;
    private String currentHash;
    private LocalDateTime createdAt;
    private Long createdBy;
}