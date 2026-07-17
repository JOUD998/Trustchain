package com.joud.trustchain.donation;

import com.joud.trustchain.blockchain_transaction.BlockchainEntityType;
import com.joud.trustchain.blockchain_transaction.BlockchainTransactionService;
import com.joud.trustchain.blockchain_transaction.BlockchainTransactionType;
import com.joud.trustchain.campaign.Campaign;
import com.joud.trustchain.campaign.CampaignRepository;
import com.joud.trustchain.campaign.CampaignStatus;
import com.joud.trustchain.donation.dto.DonationRequest;
import com.joud.trustchain.donation.dto.DonationResponse;
import com.joud.trustchain.security.CurrentUserService;
import com.joud.trustchain.user.Role;
import com.joud.trustchain.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DonationServiceTest {

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private DonationRepository donationRepository;

    @Mock
    private CampaignRepository campaignRepository;

    @Mock
    private BlockchainTransactionService blockchainTransactionService;

    @InjectMocks
    private DonationService donationService;


    @Test
    void shouldCreateDonationSuccessfully() {

        // Arrange
        User donor = createDonor();

        Campaign campaign = createCampaign(
                CampaignStatus.ACTIVE,
                new BigDecimal("1000")
        );

        DonationRequest request = new DonationRequest();
        request.setCampaignId(1L);
        request.setAmount(new BigDecimal("250"));

        when(currentUserService.getCurrentUser())
                .thenReturn(donor);

        when(campaignRepository.findById(1L))
                .thenReturn(Optional.of(campaign));

        when(donationRepository.save(any(Donation.class)))
                .thenAnswer(invocation -> {
                    Donation donation = invocation.getArgument(0);
                    donation.setId(10L);
                    return donation;
                });

        // Act
        DonationResponse result =
                donationService.createDonation(request);

        // Assert: Response
        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals(new BigDecimal("250"), result.getAmount());
        assertEquals(1L, result.getCampaignId());
        assertEquals("Food Campaign", result.getCampaignTitle());
        assertEquals(5L, result.getUserId());
        assertEquals("Test Donor", result.getUserFullName());
        assertNotNull(result.getCreatedAt());

        // Assert: campaign amount was increased
        assertEquals(
                new BigDecimal("1250"),
                campaign.getCurrentAmount()
        );

        // Capture the Donation passed to save()
        ArgumentCaptor<Donation> donationCaptor =
                ArgumentCaptor.forClass(Donation.class);

        verify(donationRepository).save(donationCaptor.capture());

        Donation savedDonation = donationCaptor.getValue();

        assertEquals(new BigDecimal("250"), savedDonation.getAmount());
        assertEquals(donor, savedDonation.getUser());
        assertEquals(campaign, savedDonation.getCampaign());
        assertNotNull(savedDonation.getCreatedAt());

        verify(currentUserService).getCurrentUser();
        verify(campaignRepository).findById(1L);

        verify(blockchainTransactionService).recordTransaction(
                BlockchainTransactionType.DONATION_CREATED,
                BlockchainEntityType.DONATION,
                10L,
                "User 5 donated 250 to campaign 1",
                5L
        );
    }


    @Test
    void shouldThrowExceptionWhenCampaignDoesNotExist() {

        // Arrange
        User donor = createDonor();

        DonationRequest request = new DonationRequest();
        request.setCampaignId(99L);
        request.setAmount(new BigDecimal("250"));

        when(currentUserService.getCurrentUser())
                .thenReturn(donor);

        when(campaignRepository.findById(99L))
                .thenReturn(Optional.empty());

        // Act + Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> donationService.createDonation(request)
        );

        assertEquals("Campaign not found", exception.getMessage());

        verify(currentUserService).getCurrentUser();
        verify(campaignRepository).findById(99L);

        verify(donationRepository, never())
                .save(any(Donation.class));

        verifyNoInteractions(blockchainTransactionService);
    }


    @Test
    void shouldThrowExceptionWhenCampaignIsCompleted() {

        // Arrange
        User donor = createDonor();

        Campaign campaign = createCampaign(
                CampaignStatus.COMPLETED,
                new BigDecimal("5000")
        );

        DonationRequest request = new DonationRequest();
        request.setCampaignId(1L);
        request.setAmount(new BigDecimal("250"));

        when(currentUserService.getCurrentUser())
                .thenReturn(donor);

        when(campaignRepository.findById(1L))
                .thenReturn(Optional.of(campaign));

        // Act + Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> donationService.createDonation(request)
        );

        assertEquals(
                "You can't Donate, Campaign is Completed",
                exception.getMessage()
        );

        // Amount must remain unchanged
        assertEquals(
                new BigDecimal("5000"),
                campaign.getCurrentAmount()
        );

        verify(currentUserService).getCurrentUser();
        verify(campaignRepository).findById(1L);

        verify(donationRepository, never())
                .save(any(Donation.class));

        verifyNoInteractions(blockchainTransactionService);
    }


    @Test
    void shouldThrowExceptionWhenCampaignIsCanceled() {

        // Arrange
        User donor = createDonor();

        Campaign campaign = createCampaign(
                CampaignStatus.CANCELED,
                new BigDecimal("1000")
        );

        DonationRequest request = new DonationRequest();
        request.setCampaignId(1L);
        request.setAmount(new BigDecimal("250"));

        when(currentUserService.getCurrentUser())
                .thenReturn(donor);

        when(campaignRepository.findById(1L))
                .thenReturn(Optional.of(campaign));

        // Act + Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> donationService.createDonation(request)
        );

        assertEquals(
                "You can't Donate, Campaign is Canceled",
                exception.getMessage()
        );

        // Amount must remain unchanged
        assertEquals(
                new BigDecimal("1000"),
                campaign.getCurrentAmount()
        );

        verify(currentUserService).getCurrentUser();
        verify(campaignRepository).findById(1L);

        verify(donationRepository, never())
                .save(any(Donation.class));

        verifyNoInteractions(blockchainTransactionService);
    }


    @Test
    void shouldReturnAllDonations() {

        // Arrange
        User donor = createDonor();

        Campaign campaign = createCampaign(
                CampaignStatus.ACTIVE,
                new BigDecimal("1500")
        );

        Donation firstDonation = createDonation(
                10L,
                new BigDecimal("250"),
                campaign,
                donor
        );

        Donation secondDonation = createDonation(
                11L,
                new BigDecimal("500"),
                campaign,
                donor
        );

        when(donationRepository.findAll())
                .thenReturn(List.of(firstDonation, secondDonation));

        // Act
        List<DonationResponse> result =
                donationService.getAllDonations();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        DonationResponse firstResult = result.get(0);
        DonationResponse secondResult = result.get(1);

        assertEquals(10L, firstResult.getId());
        assertEquals(new BigDecimal("250"), firstResult.getAmount());
        assertEquals(1L, firstResult.getCampaignId());
        assertEquals("Food Campaign", firstResult.getCampaignTitle());
        assertEquals(5L, firstResult.getUserId());
        assertEquals("Test Donor", firstResult.getUserFullName());

        assertEquals(11L, secondResult.getId());
        assertEquals(new BigDecimal("500"), secondResult.getAmount());

        verify(donationRepository).findAll();
    }


    @Test
    void shouldReturnEmptyListWhenNoDonationsExist() {

        // Arrange
        when(donationRepository.findAll())
                .thenReturn(List.of());

        // Act
        List<DonationResponse> result =
                donationService.getAllDonations();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(donationRepository).findAll();
    }


    @Test
    void shouldReturnDonationsByCampaignId() {

        // Arrange
        Long campaignId = 1L;

        User donor = createDonor();

        Campaign campaign = createCampaign(
                CampaignStatus.ACTIVE,
                new BigDecimal("1500")
        );

        Donation firstDonation = createDonation(
                10L,
                new BigDecimal("250"),
                campaign,
                donor
        );

        Donation secondDonation = createDonation(
                11L,
                new BigDecimal("500"),
                campaign,
                donor
        );

        when(campaignRepository.existsById(campaignId))
                .thenReturn(true);

        when(donationRepository.findAllByCampaign_Id(campaignId))
                .thenReturn(List.of(firstDonation, secondDonation));

        // Act
        List<DonationResponse> result =
                donationService.getDonationsByCampaignId(campaignId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals(10L, result.get(0).getId());
        assertEquals(new BigDecimal("250"), result.get(0).getAmount());
        assertEquals(campaignId, result.get(0).getCampaignId());

        assertEquals(11L, result.get(1).getId());
        assertEquals(new BigDecimal("500"), result.get(1).getAmount());
        assertEquals(campaignId, result.get(1).getCampaignId());

        verify(campaignRepository).existsById(campaignId);
        verify(donationRepository)
                .findAllByCampaign_Id(campaignId);
    }


    @Test
    void shouldThrowExceptionWhenGettingDonationsForMissingCampaign() {

        // Arrange
        Long campaignId = 99L;

        when(campaignRepository.existsById(campaignId))
                .thenReturn(false);

        // Act + Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> donationService.getDonationsByCampaignId(campaignId)
        );

        assertEquals("Campaign not found", exception.getMessage());

        verify(campaignRepository).existsById(campaignId);

        verify(donationRepository, never())
                .findAllByCampaign_Id(any());
    }


    /*
     * Helper methods:
     * These reduce duplicated test setup code.
     */

    private User createDonor() {
        User donor = new User();
        donor.setId(5L);
        donor.setFullName("Test Donor");
        donor.setRole(Role.DONOR);

        return donor;
    }

    private Campaign createCampaign(
            CampaignStatus status,
            BigDecimal currentAmount
    ) {
        return Campaign.builder()
                .id(1L)
                .title("Food Campaign")
                .description("Help families with food")
                .currentAmount(currentAmount)
                .targetAmount(new BigDecimal("5000"))
                .status(status)
                .build();
    }

    private Donation createDonation(
            Long id,
            BigDecimal amount,
            Campaign campaign,
            User user
    ) {
        return Donation.builder()
                .id(id)
                .amount(amount)
                .createdAt(LocalDateTime.now())
                .campaign(campaign)
                .user(user)
                .build();
    }
}