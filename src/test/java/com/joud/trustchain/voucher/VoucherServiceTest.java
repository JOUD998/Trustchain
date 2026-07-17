package com.joud.trustchain.voucher;

import com.joud.trustchain.blockchain_transaction.BlockchainEntityType;
import com.joud.trustchain.blockchain_transaction.BlockchainTransactionService;
import com.joud.trustchain.blockchain_transaction.BlockchainTransactionType;
import com.joud.trustchain.campaign.Campaign;
import com.joud.trustchain.campaign.CampaignRepository;
import com.joud.trustchain.security.CurrentUserService;
import com.joud.trustchain.user.User;
import com.joud.trustchain.user.UserRepository;
import com.joud.trustchain.voucher.dto.CreateVoucherRequest;
import com.joud.trustchain.voucher.dto.VerifyVoucherResponse;
import com.joud.trustchain.voucher.dto.VoucherResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VoucherServiceTest {

    @Mock
    private VoucherRepository voucherRepository;

    @Mock
    private CampaignRepository campaignRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BlockchainTransactionService blockchainTransactionService;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private VoucherService voucherService;


    @Test
    void shouldCreateVoucherSuccessfully() {

        // Arrange
        Campaign campaign = createCampaign();
        User beneficiary = createBeneficiary();
        User currentUser = createCurrentUser();

        LocalDateTime expiresAt = LocalDateTime.now().plusDays(30);

        CreateVoucherRequest request = createVoucherRequest(expiresAt);

        when(campaignRepository.getById(1L))
                .thenReturn(campaign);

        when(userRepository.getById(20L))
                .thenReturn(beneficiary);

        when(voucherRepository.existsByCode(anyString()))
                .thenReturn(false);

        when(voucherRepository.save(any(Voucher.class)))
                .thenAnswer(invocation -> {
                    Voucher voucher = invocation.getArgument(0);
                    voucher.setId(100L);
                    return voucher;
                });

        when(currentUserService.getCurrentUser())
                .thenReturn(currentUser);

        // Act
        VoucherResponse result =
                voucherService.createVoucher(request);

        // Assert: response
        assertNotNull(result);
        assertEquals(100L, result.getId());

        assertNotNull(result.getCode());
        assertTrue(result.getCode().matches("VCH-[A-Z0-9]{8}"));

        assertEquals(new BigDecimal("150.00"), result.getAmount());
        assertEquals(VoucherStatus.ISSUED, result.getStatus());
        assertNotNull(result.getIssuedAt());
        assertEquals(expiresAt, result.getExpiresAt());
        assertNull(result.getRedeemedAt());

        assertEquals(1L, result.getCampaignId());
        assertEquals("Food Campaign", result.getCampaignTitle());

        assertEquals(20L, result.getBeneficiaryId());
        assertEquals("Test Beneficiary", result.getBeneficiaryFullName());

        // Assert: saved voucher
        ArgumentCaptor<Voucher> voucherCaptor =
                ArgumentCaptor.forClass(Voucher.class);

        verify(voucherRepository).save(voucherCaptor.capture());

        Voucher savedVoucher = voucherCaptor.getValue();

        assertEquals(new BigDecimal("150.00"), savedVoucher.getAmount());
        assertEquals(VoucherStatus.ISSUED, savedVoucher.getStatus());
        assertEquals(campaign, savedVoucher.getCampaign());
        assertEquals(beneficiary, savedVoucher.getBeneficiary());
        assertEquals(expiresAt, savedVoucher.getExpiresAt());
        assertNotNull(savedVoucher.getIssuedAt());

        String generatedCode = savedVoucher.getCode();

        assertNotNull(generatedCode);
        assertTrue(generatedCode.matches("VCH-[A-Z0-9]{8}"));

        verify(campaignRepository).getById(1L);
        verify(userRepository).getById(20L);
        verify(voucherRepository).existsByCode(generatedCode);
        verify(currentUserService).getCurrentUser();

        verify(blockchainTransactionService).recordTransaction(
                eq(BlockchainTransactionType.VOUCHER_ISSUED),
                eq(BlockchainEntityType.VOUCHER),
                eq(100L),
                eq(
                        "Voucher " + generatedCode
                                + " was issued for beneficiary 20"
                                + " from campaign 1"
                ),
                eq(99L)
        );
    }


    @Test
    void shouldGenerateAnotherCodeWhenFirstCodeAlreadyExists() {

        // Arrange
        Campaign campaign = createCampaign();
        User beneficiary = createBeneficiary();
        User currentUser = createCurrentUser();

        CreateVoucherRequest request =
                createVoucherRequest(LocalDateTime.now().plusDays(30));

        when(campaignRepository.getById(1L))
                .thenReturn(campaign);

        when(userRepository.getById(20L))
                .thenReturn(beneficiary);

        /*
         * First generated code exists.
         * Second generated code is available.
         */
        when(voucherRepository.existsByCode(anyString()))
                .thenReturn(true, false);

        when(voucherRepository.save(any(Voucher.class)))
                .thenAnswer(invocation -> {
                    Voucher voucher = invocation.getArgument(0);
                    voucher.setId(100L);
                    return voucher;
                });

        when(currentUserService.getCurrentUser())
                .thenReturn(currentUser);

        // Act
        VoucherResponse result =
                voucherService.createVoucher(request);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getCode());
        assertTrue(result.getCode().matches("VCH-[A-Z0-9]{8}"));

        verify(voucherRepository, times(2))
                .existsByCode(anyString());

        verify(voucherRepository)
                .save(any(Voucher.class));

        verify(blockchainTransactionService)
                .recordTransaction(
                        eq(BlockchainTransactionType.VOUCHER_ISSUED),
                        eq(BlockchainEntityType.VOUCHER),
                        eq(100L),
                        anyString(),
                        eq(99L)
                );
    }


    @Test
    void shouldReturnInvalidWhenVoucherDoesNotExist() {

        // Arrange
        String code = "VCH-NOTFOUND";

        when(voucherRepository.findByCode(code))
                .thenReturn(Optional.empty());

        // Act
        VerifyVoucherResponse result =
                voucherService.verifyVoucher(code);

        // Assert
        assertNotNull(result);
        assertEquals(code, result.getCode());
        assertFalse(result.isValid());
        assertEquals("Voucher not found", result.getMessage());

        verify(voucherRepository).findByCode(code);
    }


    @Test
    void shouldReturnInvalidWhenVoucherIsCancelled() {

        // Arrange
        Voucher voucher = createVoucher(
                VoucherStatus.CANCELLED,
                LocalDateTime.now().plusDays(10)
        );

        when(voucherRepository.findByCode(voucher.getCode()))
                .thenReturn(Optional.of(voucher));

        // Act
        VerifyVoucherResponse result =
                voucherService.verifyVoucher(voucher.getCode());

        // Assert
        assertFalse(result.isValid());
        assertEquals("Voucher is cancelled", result.getMessage());
        assertEquals(voucher.getCode(), result.getCode());
        assertEquals(VoucherStatus.CANCELLED, result.getStatus());
        assertEquals(voucher.getExpiresAt(), result.getExpiresAt());

        verify(voucherRepository).findByCode(voucher.getCode());
    }


    @Test
    void shouldReturnInvalidWhenVoucherIsAlreadyRedeemed() {

        // Arrange
        Voucher voucher = createVoucher(
                VoucherStatus.REDEEMED,
                LocalDateTime.now().plusDays(10)
        );

        when(voucherRepository.findByCode(voucher.getCode()))
                .thenReturn(Optional.of(voucher));

        // Act
        VerifyVoucherResponse result =
                voucherService.verifyVoucher(voucher.getCode());

        // Assert
        assertFalse(result.isValid());
        assertEquals("Voucher is already used", result.getMessage());
        assertEquals(VoucherStatus.REDEEMED, result.getStatus());

        verify(voucherRepository).findByCode(voucher.getCode());
    }


    @Test
    void shouldReturnInvalidWhenVoucherIsExpired() {

        // Arrange
        Voucher voucher = createVoucher(
                VoucherStatus.ISSUED,
                LocalDateTime.now().minusDays(1)
        );

        when(voucherRepository.findByCode(voucher.getCode()))
                .thenReturn(Optional.of(voucher));

        // Act
        VerifyVoucherResponse result =
                voucherService.verifyVoucher(voucher.getCode());

        // Assert
        assertFalse(result.isValid());
        assertEquals("Voucher is expired", result.getMessage());
        assertEquals(VoucherStatus.ISSUED, result.getStatus());

        verify(voucherRepository).findByCode(voucher.getCode());
    }


    @Test
    void shouldVerifyValidVoucher() {

        // Arrange
        Voucher voucher = createVoucher(
                VoucherStatus.ISSUED,
                LocalDateTime.now().plusDays(10)
        );

        when(voucherRepository.findByCode(voucher.getCode()))
                .thenReturn(Optional.of(voucher));

        // Act
        VerifyVoucherResponse result =
                voucherService.verifyVoucher(voucher.getCode());

        // Assert
        assertTrue(result.isValid());
        assertEquals("Voucher is valid", result.getMessage());
        assertEquals(voucher.getCode(), result.getCode());
        assertEquals(VoucherStatus.ISSUED, result.getStatus());
        assertEquals(voucher.getExpiresAt(), result.getExpiresAt());

        verify(voucherRepository).findByCode(voucher.getCode());
    }


    @Test
    void shouldRedeemVoucherSuccessfully() {

        // Arrange
        Voucher voucher = createVoucher(
                VoucherStatus.ISSUED,
                LocalDateTime.now().plusDays(10)
        );

        User currentUser = createCurrentUser();

        when(voucherRepository.findByCode(voucher.getCode()))
                .thenReturn(Optional.of(voucher));

        when(voucherRepository.save(any(Voucher.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(currentUserService.getCurrentUser())
                .thenReturn(currentUser);

        // Act
        VoucherResponse result =
                voucherService.redeemVoucher(voucher.getCode());

        // Assert: response
        assertNotNull(result);
        assertEquals(VoucherStatus.REDEEMED, result.getStatus());
        assertNotNull(result.getRedeemedAt());

        // Assert: entity changed
        assertEquals(VoucherStatus.REDEEMED, voucher.getStatus());
        assertNotNull(voucher.getRedeemedAt());

        ArgumentCaptor<Voucher> voucherCaptor =
                ArgumentCaptor.forClass(Voucher.class);

        verify(voucherRepository).save(voucherCaptor.capture());

        Voucher savedVoucher = voucherCaptor.getValue();

        assertEquals(VoucherStatus.REDEEMED, savedVoucher.getStatus());
        assertNotNull(savedVoucher.getRedeemedAt());

        verify(currentUserService).getCurrentUser();

        verify(blockchainTransactionService).recordTransaction(
                BlockchainTransactionType.VOUCHER_REDEEMED,
                BlockchainEntityType.VOUCHER,
                100L,
                "Voucher VCH-ABC12345"
                        + " was redeemed by user 99"
                        + " for beneficiary 20"
                        + " from campaign 1",
                99L
        );
    }


    @Test
    void shouldThrowExceptionWhenRedeemingMissingVoucher() {

        // Arrange
        String code = "VCH-NOTFOUND";

        when(voucherRepository.findByCode(code))
                .thenReturn(Optional.empty());

        // Act + Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> voucherService.redeemVoucher(code)
        );

        assertEquals("Voucher not found", exception.getMessage());

        verify(voucherRepository).findByCode(code);

        verify(voucherRepository, never())
                .save(any(Voucher.class));

        verifyNoInteractions(
                currentUserService,
                blockchainTransactionService
        );
    }


    @Test
    void shouldThrowExceptionWhenRedeemingAlreadyUsedVoucher() {

        // Arrange
        Voucher voucher = createVoucher(
                VoucherStatus.REDEEMED,
                LocalDateTime.now().plusDays(10)
        );

        when(voucherRepository.findByCode(voucher.getCode()))
                .thenReturn(Optional.of(voucher));

        // Act + Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> voucherService.redeemVoucher(voucher.getCode())
        );

        assertEquals(
                "Voucher is already used",
                exception.getMessage()
        );

        verify(voucherRepository).findByCode(voucher.getCode());

        verify(voucherRepository, never())
                .save(any(Voucher.class));

        verifyNoInteractions(
                currentUserService,
                blockchainTransactionService
        );
    }


    @Test
    void shouldThrowExceptionWhenRedeemingCancelledVoucher() {

        // Arrange
        Voucher voucher = createVoucher(
                VoucherStatus.CANCELLED,
                LocalDateTime.now().plusDays(10)
        );

        when(voucherRepository.findByCode(voucher.getCode()))
                .thenReturn(Optional.of(voucher));

        // Act + Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> voucherService.redeemVoucher(voucher.getCode())
        );

        assertEquals(
                "Voucher is cancelled",
                exception.getMessage()
        );

        verify(voucherRepository).findByCode(voucher.getCode());

        verify(voucherRepository, never())
                .save(any(Voucher.class));

        verifyNoInteractions(
                currentUserService,
                blockchainTransactionService
        );
    }


    @Test
    void shouldThrowExceptionWhenRedeemingExpiredVoucher() {

        // Arrange
        Voucher voucher = createVoucher(
                VoucherStatus.ISSUED,
                LocalDateTime.now().minusDays(1)
        );

        when(voucherRepository.findByCode(voucher.getCode()))
                .thenReturn(Optional.of(voucher));

        // Act + Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> voucherService.redeemVoucher(voucher.getCode())
        );

        assertEquals(
                "Voucher is expired",
                exception.getMessage()
        );

        verify(voucherRepository).findByCode(voucher.getCode());

        verify(voucherRepository, never())
                .save(any(Voucher.class));

        verifyNoInteractions(
                currentUserService,
                blockchainTransactionService
        );
    }


    /*
     * Helper methods
     */

    private CreateVoucherRequest createVoucherRequest(
            LocalDateTime expiresAt
    ) {
        CreateVoucherRequest request = new CreateVoucherRequest();

        request.setAmount(new BigDecimal("150.00"));
        request.setCampaignId(1L);
        request.setBeneficiaryId(20L);
        request.setExpiresAt(expiresAt);

        return request;
    }


    private Campaign createCampaign() {
        return Campaign.builder()
                .id(1L)
                .title("Food Campaign")
                .build();
    }


    private User createBeneficiary() {
        User beneficiary = new User();
        beneficiary.setId(20L);
        beneficiary.setFullName("Test Beneficiary");

        return beneficiary;
    }


    private User createCurrentUser() {
        User currentUser = new User();
        currentUser.setId(99L);
        currentUser.setFullName("Organization User");

        return currentUser;
    }


    private Voucher createVoucher(
            VoucherStatus status,
            LocalDateTime expiresAt
    ) {
        return Voucher.builder()
                .id(100L)
                .code("VCH-ABC12345")
                .amount(new BigDecimal("150.00"))
                .status(status)
                .issuedAt(LocalDateTime.now().minusDays(1))
                .expiresAt(expiresAt)
                .redeemedAt(
                        status == VoucherStatus.REDEEMED
                                ? LocalDateTime.now().minusHours(1)
                                : null
                )
                .campaign(createCampaign())
                .beneficiary(createBeneficiary())
                .build();
    }
}