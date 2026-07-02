package com.joud.trustchain.blockchain_transaction;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "blockchain_transaction")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BlockchainTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BlockchainTransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BlockchainEntityType entityType;

    @Column(nullable = false)
    private Long entityId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String data;

    @Column(nullable = false)
    private String previousHash;

    @Column(nullable = false)
    private String currentHash;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private Long createdBy;
}