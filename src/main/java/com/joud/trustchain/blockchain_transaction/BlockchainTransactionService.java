package com.joud.trustchain.blockchain_transaction;

import com.joud.trustchain.blockchain_transaction.dto.BlockchainTransactionResponse;
import com.joud.trustchain.blockchain_transaction.dto.BlockchainVerificationResponse;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class BlockchainTransactionService {

    private final BlockchainTransactionRepository blockchainTransactionRepository;

    public BlockchainTransactionService(BlockchainTransactionRepository blockchainTransactionRepository) {
        this.blockchainTransactionRepository = blockchainTransactionRepository;
    }


    public List<BlockchainTransactionResponse> getAllBlockchainTransactions() {

        List<BlockchainTransaction> blockchainTransactions = blockchainTransactionRepository.findAllByOrderByIdAsc();
        List<BlockchainTransactionResponse> blockchainTransactionResponses = new ArrayList<>();

        for (BlockchainTransaction blockchainTransaction : blockchainTransactions) {

            BlockchainTransactionResponse blockchainTransactionResponse = mapToBlockchainTransactionResponse(blockchainTransaction);
            blockchainTransactionResponses.add(blockchainTransactionResponse);

        }
        return blockchainTransactionResponses;
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
            return hexString.toString();

        } catch (Exception e) {
            throw new RuntimeException("Could not calculate hash", e);
        }
    }




    private boolean isTransactionHashValid(BlockchainTransaction blockchainTransaction) {

        String calculatedHash = calculateHash(
                blockchainTransaction.getTransactionType(),
                blockchainTransaction.getEntityType(),
                blockchainTransaction.getEntityId(),
                blockchainTransaction.getData(),
                blockchainTransaction.getPreviousHash(),
                blockchainTransaction.getCreatedAt()
        );

        return calculatedHash.equals(blockchainTransaction.getCurrentHash());
    }

    public BlockchainVerificationResponse verifyBlockchainIntegrity() {
        List<BlockchainTransaction> blockchainTransactions =
                blockchainTransactionRepository.findAllByOrderByIdAsc();

        for (int i = 0; i < blockchainTransactions.size(); i++) {
            BlockchainTransaction currentTransaction = blockchainTransactions.get(i);

            if (!isTransactionHashValid(currentTransaction)) {
                return new BlockchainVerificationResponse(false,"Transaction hash is not valid.",currentTransaction.getId());
            }

            if (i == 0) {
                if (!currentTransaction.getPreviousHash().equals("GENESIS")) {
                    return new BlockchainVerificationResponse(false,"First transaction previous hash is not GENESIS.",currentTransaction.getId());
                }
            } else {
                BlockchainTransaction previousTransaction = blockchainTransactions.get(i - 1);

                if (!currentTransaction.getPreviousHash().equals(previousTransaction.getCurrentHash())) {
                    return new BlockchainVerificationResponse(false,"Invalid chain link: the previous transaction hash does not match the current transaction's previous hash.",currentTransaction.getId());
                }
            }
        }

        return new BlockchainVerificationResponse(true,"Blockchain chain is valid.",null);
    }


    private BlockchainTransactionResponse mapToBlockchainTransactionResponse(BlockchainTransaction blockchainTransaction) {
        BlockchainTransactionResponse blockchainTransactionResponse = new BlockchainTransactionResponse();

        blockchainTransactionResponse.setId(blockchainTransaction.getId());
        blockchainTransactionResponse.setTransactionType(blockchainTransaction.getTransactionType());
        blockchainTransactionResponse.setEntityType(blockchainTransaction.getEntityType());
        blockchainTransactionResponse.setEntityId(blockchainTransaction.getEntityId());
        blockchainTransactionResponse.setData(blockchainTransaction.getData());
        blockchainTransactionResponse.setPreviousHash(blockchainTransaction.getPreviousHash());
        blockchainTransactionResponse.setCurrentHash(blockchainTransaction.getCurrentHash());
        blockchainTransactionResponse.setCreatedAt(blockchainTransaction.getCreatedAt());
        blockchainTransactionResponse.setCreatedBy(blockchainTransaction.getCreatedBy());

        return blockchainTransactionResponse;
    }

}