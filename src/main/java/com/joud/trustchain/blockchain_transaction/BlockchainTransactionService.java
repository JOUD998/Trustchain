package com.joud.trustchain.blockchain_transaction;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;

@Service
public class BlockchainTransactionService {

    private final BlockchainTransactionRepository blockchainTransactionRepository;

    public BlockchainTransactionService(BlockchainTransactionRepository blockchainTransactionRepository) {
        this.blockchainTransactionRepository = blockchainTransactionRepository;
    }

    public BlockchainTransaction recordTransaction(
            BlockchainTransactionType transactionType,
            BlockchainEntityType entityType,
            Long entityId,
            String data,
            Long createdBy
    ) {
        String previousHash = blockchainTransactionRepository
                .findTopByOrderByIdDesc()
                .map(BlockchainTransaction::getCurrentHash)
                .orElse("GENESIS");

        LocalDateTime createdAt = LocalDateTime.now();

        String currentHash = calculateHash(
                transactionType,
                entityType,
                entityId,
                data,
                previousHash,
                createdAt
        );

        BlockchainTransaction transaction = BlockchainTransaction.builder()
                .transactionType(transactionType)
                .entityType(entityType)
                .entityId(entityId)
                .data(data)
                .previousHash(previousHash)
                .currentHash(currentHash)
                .createdAt(createdAt)
                .createdBy(createdBy)
                .build();

        return blockchainTransactionRepository.save(transaction);
    }

    private String calculateHash(
            BlockchainTransactionType transactionType,
            BlockchainEntityType entityType,
            Long entityId,
            String data,
            String previousHash,
            LocalDateTime createdAt
    ) {
        try {
            String input = transactionType
                    + "|" + entityType
                    + "|" + entityId
                    + "|" + data
                    + "|" + previousHash
                    + "|" + createdAt;

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();

            for (byte b : hashBytes) {
                hexString.append(String.format("%02x", b));
            }
            System.out.println("Hashed: " + hexString.toString());

            return hexString.toString();

        } catch (Exception e) {
            throw new RuntimeException("Could not calculate hash", e);
        }
    }
}