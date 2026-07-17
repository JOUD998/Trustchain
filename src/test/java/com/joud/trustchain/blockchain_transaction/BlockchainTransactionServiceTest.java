package com.joud.trustchain.blockchain_transaction;

import com.joud.trustchain.blockchain_transaction.dto.BlockchainVerificationResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlockchainTransactionServiceTest {

    @Mock
    private BlockchainTransactionRepository blockchainTransactionRepository;

    @InjectMocks
    private BlockchainTransactionService blockchainTransactionService;

    @Test
    void shouldCreateGenesisTransaction() {

        // Arrange
        BlockchainTransactionType transactionType =
                BlockchainTransactionType.CAMPAIGN_CREATED;

        BlockchainEntityType entityType =
                BlockchainEntityType.CAMPAIGN;

        Long entityId = 1L;
        String data = "Campaign 1 was created";
        Long createdBy = 5L;

        when(blockchainTransactionRepository.findTopByOrderByIdDesc())
                .thenReturn(Optional.empty());

        when(blockchainTransactionRepository.save(any(BlockchainTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        BlockchainTransaction result =
                blockchainTransactionService.recordTransaction(
                        transactionType,
                        entityType,
                        entityId,
                        data,
                        createdBy
                );

        // Assert
        assertNotNull(result);
        assertEquals("GENESIS", result.getPreviousHash());
        assertEquals(transactionType, result.getTransactionType());
        assertEquals(entityType, result.getEntityType());
        assertEquals(entityId, result.getEntityId());
        assertEquals(data, result.getData());
        assertEquals(createdBy, result.getCreatedBy());

        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getCurrentHash());
        assertEquals(64, result.getCurrentHash().length());

        String expectedHash = calculateExpectedHash(
                transactionType,
                entityType,
                entityId,
                data,
                "GENESIS",
                result.getCreatedAt()
        );

        assertEquals(expectedHash, result.getCurrentHash());

        verify(blockchainTransactionRepository)
                .findTopByOrderByIdDesc();

        verify(blockchainTransactionRepository)
                .save(any(BlockchainTransaction.class));
    }

    @Test
    void shouldLinkTransactionToPreviousHash() {

        // Arrange
        BlockchainTransaction previousTransaction = BlockchainTransaction.builder()
                .id(1L)
                .currentHash("PREVIOUS_HASH_123")
                .build();

        when(blockchainTransactionRepository.findTopByOrderByIdDesc())
                .thenReturn(Optional.of(previousTransaction));

        when(blockchainTransactionRepository.save(any(BlockchainTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        BlockchainTransaction result =
                blockchainTransactionService.recordTransaction(
                        BlockchainTransactionType.CAMPAIGN_UPDATED,
                        BlockchainEntityType.CAMPAIGN,
                        2L,
                        "Campaign updated",
                        5L
                );

        // Assert
        assertEquals("PREVIOUS_HASH_123", result.getPreviousHash());

        assertNotEquals(
                previousTransaction.getCurrentHash(),
                result.getCurrentHash()
        );

        verify(blockchainTransactionRepository)
                .findTopByOrderByIdDesc();

        verify(blockchainTransactionRepository)
                .save(any(BlockchainTransaction.class));
    }

    @Test
    void shouldDetectTamperedBlockchain() {

        when(blockchainTransactionRepository.findTopByOrderByIdDesc())
                .thenReturn(Optional.empty());

        when(blockchainTransactionRepository.save(any(BlockchainTransaction.class)))
                .thenAnswer(invocation -> {
                    BlockchainTransaction transaction = invocation.getArgument(0);
                    transaction.setId(1L);
                    return transaction;
                });

        BlockchainTransaction transaction =
                blockchainTransactionService.recordTransaction(
                        BlockchainTransactionType.CAMPAIGN_CREATED,
                        BlockchainEntityType.CAMPAIGN,
                        1L,
                        "Campaign 1 was created",
                        5L
                );

        transaction.setData("TAMPERED DATA");

        when(blockchainTransactionRepository.findAllByOrderByIdAsc())
                .thenReturn(List.of(transaction));

        // Act
        BlockchainVerificationResponse result =
                blockchainTransactionService.verifyBlockchainIntegrity();

        // Assert
        assertFalse(result.isValid());
        assertEquals(
                "Transaction hash is not valid.",
                result.getMessage()
        );
        assertEquals(1L, result.getTransactionId());
        verify(blockchainTransactionRepository)
                .findAllByOrderByIdAsc();
    }

    @Test
    void shouldVerifyValidBlockchain() {

        // Arrange

        // أول Transaction (Genesis)
        when(blockchainTransactionRepository.findTopByOrderByIdDesc())
                .thenReturn(Optional.empty());

        when(blockchainTransactionRepository.save(any(BlockchainTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BlockchainTransaction first =
                blockchainTransactionService.recordTransaction(
                        BlockchainTransactionType.CAMPAIGN_CREATED,
                        BlockchainEntityType.CAMPAIGN,
                        1L,
                        "Campaign created",
                        5L
                );

        // ثاني Transaction مرتبط بالأول
        when(blockchainTransactionRepository.findTopByOrderByIdDesc())
                .thenReturn(Optional.of(first));

        BlockchainTransaction second =
                blockchainTransactionService.recordTransaction(
                        BlockchainTransactionType.CAMPAIGN_UPDATED,
                        BlockchainEntityType.CAMPAIGN,
                        1L,
                        "Campaign updated",
                        5L
                );

        when(blockchainTransactionRepository.findAllByOrderByIdAsc())
                .thenReturn(List.of(first, second));

        // Act
        BlockchainVerificationResponse result =
                blockchainTransactionService.verifyBlockchainIntegrity();

        // Assert
        assertTrue(result.isValid());
        assertEquals("Blockchain chain is valid.", result.getMessage());
        assertNull(result.getTransactionId());

        verify(blockchainTransactionRepository, times(2))
                .findTopByOrderByIdDesc();

        verify(blockchainTransactionRepository, times(2))
                .save(any(BlockchainTransaction.class));

        verify(blockchainTransactionRepository)
                .findAllByOrderByIdAsc();
    }




    private String calculateExpectedHash(
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

            byte[] hashBytes =
                    digest.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();

            for (byte b : hashBytes) {
                hexString.append(String.format("%02x", b));
            }

            return hexString.toString();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}