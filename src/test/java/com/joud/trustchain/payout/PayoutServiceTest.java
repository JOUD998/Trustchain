package com.joud.trustchain.payout;

import com.joud.trustchain.blockchain_transaction.BlockchainEntityType;
import com.joud.trustchain.blockchain_transaction.BlockchainTransactionService;
import com.joud.trustchain.blockchain_transaction.BlockchainTransactionType;
import com.joud.trustchain.campaign.Campaign;
import com.joud.trustchain.milestone.Milestone;
import com.joud.trustchain.milestone.MilestoneService;
import com.joud.trustchain.milestone.MilestoneStatus;
import com.joud.trustchain.payout.dto.CreatePayoutRequest;
import com.joud.trustchain.payout.dto.PayoutResponse;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PayoutServiceTest {

    @Mock
    private PayoutRepository payoutRepository;

    @Mock
    private MilestoneService milestoneService;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private BlockchainTransactionService blockchainTransactionService;

    @InjectMocks
    private PayoutService payoutService;


    @Test
    void shouldCreatePayoutSuccessfullyAsAdmin() {

        // Arrange
        User admin = createUser(99L, Role.ADMIN);
        Campaign campaign = createCampaign();
        Milestone milestone =
                createMilestone(campaign, MilestoneStatus.APPROVED);

        CreatePayoutRequest request = createPayoutRequest();

        when(currentUserService.getCurrentUser())
                .thenReturn(admin);

        when(milestoneService.findMilestoneEntityById(10L))
                .thenReturn(milestone);

        when(payoutRepository.existsByMilestone_Id(10L))
                .thenReturn(false);

        when(payoutRepository.findAllByMilestone_Campaign_Id(1L))
                .thenReturn(List.of());

        when(payoutRepository.save(any(Payout.class)))
                .thenAnswer(invocation -> {
                    Payout payout = invocation.getArgument(0);
                    payout.setId(100L);
                    return payout;
                });

        // Act
        PayoutResponse result =
                payoutService.createPayout(request);

        // Assert: response
        assertNotNull(result);
        assertEquals(100L, result.getId());

        assertEquals(10L, result.getMilestoneId());
        assertEquals("First Milestone", result.getMilestoneTitle());

        assertEquals(1L, result.getCampaignId());
        assertEquals("Food Campaign", result.getCampaignTitle());

        assertEquals(new BigDecimal("1000.00"), result.getAmount());
        assertEquals(PayoutStatus.EXECUTED, result.getStatus());

        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getExecutedAt());
        assertEquals(99L, result.getCreatedBy());

        // Milestone must become completed
        assertEquals(
                MilestoneStatus.COMPLETED,
                milestone.getStatus()
        );

        assertNotNull(milestone.getUpdatedAt());

        // Capture saved payout
        ArgumentCaptor<Payout> payoutCaptor =
                ArgumentCaptor.forClass(Payout.class);

        verify(payoutRepository)
                .save(payoutCaptor.capture());

        Payout savedPayout = payoutCaptor.getValue();

        assertEquals(milestone, savedPayout.getMilestone());
        assertEquals(new BigDecimal("1000.00"), savedPayout.getAmount());
        assertEquals(PayoutStatus.EXECUTED, savedPayout.getStatus());
        assertEquals(99L, savedPayout.getCreatedBy());
        assertNotNull(savedPayout.getCreatedAt());
        assertNotNull(savedPayout.getExecutedAt());

        verify(blockchainTransactionService)
                .recordTransaction(
                        BlockchainTransactionType.PAYOUT_EXECUTED,
                        BlockchainEntityType.PAYOUT,
                        100L,
                        "Payout of 1000.00 was executed for milestone 10"
                                + " of campaign 1",
                        99L
                );
    }


    @Test
    void shouldCreatePayoutSuccessfullyAsCampaignOwner() {

        // Arrange
        User organization = createUser(50L, Role.ORGANIZATION);

        Campaign campaign = createCampaign();

        /*
         * Campaign owner has the same ID as current organization user.
         */
        campaign.setOrganization(organization);

        Milestone milestone =
                createMilestone(campaign, MilestoneStatus.APPROVED);

        CreatePayoutRequest request = createPayoutRequest();

        when(currentUserService.getCurrentUser())
                .thenReturn(organization);

        when(milestoneService.findMilestoneEntityById(10L))
                .thenReturn(milestone);

        when(payoutRepository.existsByMilestone_Id(10L))
                .thenReturn(false);

        when(payoutRepository.findAllByMilestone_Campaign_Id(1L))
                .thenReturn(List.of());

        when(payoutRepository.save(any(Payout.class)))
                .thenAnswer(invocation -> {
                    Payout payout = invocation.getArgument(0);
                    payout.setId(100L);
                    return payout;
                });

        // Act
        PayoutResponse result =
                payoutService.createPayout(request);

        // Assert
        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(50L, result.getCreatedBy());

        assertEquals(
                MilestoneStatus.COMPLETED,
                milestone.getStatus()
        );

        verify(payoutRepository)
                .save(any(Payout.class));

        verify(blockchainTransactionService)
                .recordTransaction(
                        eq(BlockchainTransactionType.PAYOUT_EXECUTED),
                        eq(BlockchainEntityType.PAYOUT),
                        eq(100L),
                        anyString(),
                        eq(50L)
                );
    }


    @Test
    void shouldThrowExceptionWhenUserIsNotAuthorized() {

        // Arrange
        User organization = createUser(50L, Role.ORGANIZATION);
        User campaignOwner = createUser(60L, Role.ORGANIZATION);

        Campaign campaign = createCampaign();
        campaign.setOrganization(campaignOwner);

        Milestone milestone =
                createMilestone(campaign, MilestoneStatus.APPROVED);

        CreatePayoutRequest request = createPayoutRequest();

        when(currentUserService.getCurrentUser())
                .thenReturn(organization);

        when(milestoneService.findMilestoneEntityById(10L))
                .thenReturn(milestone);

        // Act + Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> payoutService.createPayout(request)
        );

        assertEquals(
                "You are not authorized to create payouts for this campaign",
                exception.getMessage()
        );

        verify(currentUserService).getCurrentUser();
        verify(milestoneService).findMilestoneEntityById(10L);

        verify(payoutRepository, never())
                .save(any(Payout.class));

        verifyNoInteractions(blockchainTransactionService);
    }


    @Test
    void shouldThrowExceptionWhenMilestoneIsNotApproved() {

        // Arrange
        User admin = createUser(99L, Role.ADMIN);
        Campaign campaign = createCampaign();

        Milestone milestone =
                createMilestone(campaign, MilestoneStatus.PENDING);

        CreatePayoutRequest request = createPayoutRequest();

        when(currentUserService.getCurrentUser())
                .thenReturn(admin);

        when(milestoneService.findMilestoneEntityById(10L))
                .thenReturn(milestone);

        // Act + Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> payoutService.createPayout(request)
        );

        assertEquals(
                "Payout is only allowed for approved milestones",
                exception.getMessage()
        );

        assertEquals(
                MilestoneStatus.PENDING,
                milestone.getStatus()
        );

        verify(payoutRepository, never())
                .save(any(Payout.class));

        verifyNoInteractions(blockchainTransactionService);
    }


    @Test
    void shouldThrowExceptionWhenCampaignFundingTargetNotReached() {

        // Arrange
        User admin = createUser(99L, Role.ADMIN);

        Campaign campaign = createCampaign();
        campaign.setCurrentAmount(new BigDecimal("4000.00"));
        campaign.setTargetAmount(new BigDecimal("5000.00"));

        Milestone milestone =
                createMilestone(campaign, MilestoneStatus.APPROVED);

        CreatePayoutRequest request = createPayoutRequest();

        when(currentUserService.getCurrentUser())
                .thenReturn(admin);

        when(milestoneService.findMilestoneEntityById(10L))
                .thenReturn(milestone);

        // Act + Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> payoutService.createPayout(request)
        );

        assertEquals(
                "The campaign funding target has not been reached yet",
                exception.getMessage()
        );

        verify(payoutRepository, never())
                .save(any(Payout.class));

        verifyNoInteractions(blockchainTransactionService);
    }


    @Test
    void shouldThrowExceptionWhenPayoutAlreadyExists() {

        // Arrange
        User admin = createUser(99L, Role.ADMIN);
        Campaign campaign = createCampaign();

        Milestone milestone =
                createMilestone(campaign, MilestoneStatus.APPROVED);

        CreatePayoutRequest request = createPayoutRequest();

        when(currentUserService.getCurrentUser())
                .thenReturn(admin);

        when(milestoneService.findMilestoneEntityById(10L))
                .thenReturn(milestone);

        when(payoutRepository.existsByMilestone_Id(10L))
                .thenReturn(true);

        // Act + Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> payoutService.createPayout(request)
        );

        assertEquals(
                "Payout already exists for this milestone",
                exception.getMessage()
        );

        verify(payoutRepository)
                .existsByMilestone_Id(10L);

        verify(payoutRepository, never())
                .save(any(Payout.class));

        verifyNoInteractions(blockchainTransactionService);
    }


    @Test
    void shouldThrowExceptionWhenAvailableFundsAreInsufficient() {

        // Arrange
        User admin = createUser(99L, Role.ADMIN);
        Campaign campaign = createCampaign();

        Milestone milestone =
                createMilestone(campaign, MilestoneStatus.APPROVED);

        /*
         * Campaign current amount = 5000
         * Already paid = 4500
         * Available amount = 500
         * Milestone amount = 1000
         */
        Payout previousPayout = Payout.builder()
                .id(90L)
                .milestone(
                        createMilestone(
                                campaign,
                                MilestoneStatus.COMPLETED
                        )
                )
                .amount(new BigDecimal("4500.00"))
                .status(PayoutStatus.EXECUTED)
                .createdAt(LocalDateTime.now().minusDays(1))
                .executedAt(LocalDateTime.now().minusDays(1))
                .createdBy(99L)
                .build();

        CreatePayoutRequest request = createPayoutRequest();

        when(currentUserService.getCurrentUser())
                .thenReturn(admin);

        when(milestoneService.findMilestoneEntityById(10L))
                .thenReturn(milestone);

        when(payoutRepository.existsByMilestone_Id(10L))
                .thenReturn(false);

        when(payoutRepository.findAllByMilestone_Campaign_Id(1L))
                .thenReturn(List.of(previousPayout));

        // Act + Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> payoutService.createPayout(request)
        );

        assertEquals(
                "Insufficient available campaign funds for this payout",
                exception.getMessage()
        );

        assertEquals(
                MilestoneStatus.APPROVED,
                milestone.getStatus()
        );

        verify(payoutRepository, never())
                .save(any(Payout.class));

        verifyNoInteractions(blockchainTransactionService);
    }


    @Test
    void shouldCalculateAvailableFundsAfterPreviousPayouts() {

        // Arrange
        User admin = createUser(99L, Role.ADMIN);
        Campaign campaign = createCampaign();

        Milestone milestone =
                createMilestone(campaign, MilestoneStatus.APPROVED);

        Payout firstPayout = createExistingPayout(
                90L,
                campaign,
                new BigDecimal("1500.00")
        );

        Payout secondPayout = createExistingPayout(
                91L,
                campaign,
                new BigDecimal("2000.00")
        );

        /*
         * Campaign current amount = 5000
         * Already paid = 3500
         * Available = 1500
         * New milestone = 1000
         * Therefore payout is allowed.
         */
        CreatePayoutRequest request = createPayoutRequest();

        when(currentUserService.getCurrentUser())
                .thenReturn(admin);

        when(milestoneService.findMilestoneEntityById(10L))
                .thenReturn(milestone);

        when(payoutRepository.existsByMilestone_Id(10L))
                .thenReturn(false);

        when(payoutRepository.findAllByMilestone_Campaign_Id(1L))
                .thenReturn(List.of(firstPayout, secondPayout));

        when(payoutRepository.save(any(Payout.class)))
                .thenAnswer(invocation -> {
                    Payout payout = invocation.getArgument(0);
                    payout.setId(100L);
                    return payout;
                });

        // Act
        PayoutResponse result =
                payoutService.createPayout(request);

        // Assert
        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(new BigDecimal("1000.00"), result.getAmount());

        assertEquals(
                MilestoneStatus.COMPLETED,
                milestone.getStatus()
        );

        verify(payoutRepository)
                .save(any(Payout.class));

        verify(blockchainTransactionService)
                .recordTransaction(
                        eq(BlockchainTransactionType.PAYOUT_EXECUTED),
                        eq(BlockchainEntityType.PAYOUT),
                        eq(100L),
                        anyString(),
                        eq(99L)
                );
    }


    @Test
    void shouldReturnAllPayouts() {

        // Arrange
        Campaign campaign = createCampaign();

        Milestone firstMilestone =
                createMilestone(campaign, MilestoneStatus.COMPLETED);

        Milestone secondMilestone =
                createMilestone(campaign, MilestoneStatus.COMPLETED);

        secondMilestone.setId(11L);
        secondMilestone.setTitle("Second Milestone");

        Payout firstPayout = Payout.builder()
                .id(100L)
                .milestone(firstMilestone)
                .amount(new BigDecimal("1000.00"))
                .status(PayoutStatus.EXECUTED)
                .createdAt(LocalDateTime.now())
                .executedAt(LocalDateTime.now())
                .createdBy(99L)
                .build();

        Payout secondPayout = Payout.builder()
                .id(101L)
                .milestone(secondMilestone)
                .amount(new BigDecimal("500.00"))
                .status(PayoutStatus.EXECUTED)
                .createdAt(LocalDateTime.now())
                .executedAt(LocalDateTime.now())
                .createdBy(99L)
                .build();

        when(payoutRepository.findAll())
                .thenReturn(List.of(firstPayout, secondPayout));

        // Act
        List<PayoutResponse> result =
                payoutService.getAllPayouts();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals(100L, result.get(0).getId());
        assertEquals(10L, result.get(0).getMilestoneId());
        assertEquals("First Milestone", result.get(0).getMilestoneTitle());
        assertEquals(new BigDecimal("1000.00"), result.get(0).getAmount());

        assertEquals(101L, result.get(1).getId());
        assertEquals(11L, result.get(1).getMilestoneId());
        assertEquals("Second Milestone", result.get(1).getMilestoneTitle());
        assertEquals(new BigDecimal("500.00"), result.get(1).getAmount());

        verify(payoutRepository).findAll();
    }


    @Test
    void shouldReturnEmptyListWhenNoPayoutsExist() {

        // Arrange
        when(payoutRepository.findAll())
                .thenReturn(List.of());

        // Act
        List<PayoutResponse> result =
                payoutService.getAllPayouts();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(payoutRepository).findAll();
    }


    @Test
    void shouldReturnPayoutsByMilestoneId() {

        // Arrange
        Campaign campaign = createCampaign();
        Milestone milestone =
                createMilestone(campaign, MilestoneStatus.COMPLETED);

        Payout payout = Payout.builder()
                .id(100L)
                .milestone(milestone)
                .amount(new BigDecimal("1000.00"))
                .status(PayoutStatus.EXECUTED)
                .createdAt(LocalDateTime.now())
                .executedAt(LocalDateTime.now())
                .createdBy(99L)
                .build();

        when(payoutRepository.findAllByMilestone_Id(10L))
                .thenReturn(List.of(payout));

        // Act
        List<PayoutResponse> result =
                payoutService.getPayoutsByMilestoneId(10L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        PayoutResponse response = result.get(0);

        assertEquals(100L, response.getId());
        assertEquals(10L, response.getMilestoneId());
        assertEquals("First Milestone", response.getMilestoneTitle());
        assertEquals(1L, response.getCampaignId());
        assertEquals("Food Campaign", response.getCampaignTitle());
        assertEquals(new BigDecimal("1000.00"), response.getAmount());
        assertEquals(PayoutStatus.EXECUTED, response.getStatus());
        assertEquals(99L, response.getCreatedBy());

        verify(payoutRepository)
                .findAllByMilestone_Id(10L);
    }


    @Test
    void shouldReturnEmptyListWhenMilestoneHasNoPayouts() {

        // Arrange
        when(payoutRepository.findAllByMilestone_Id(10L))
                .thenReturn(List.of());

        // Act
        List<PayoutResponse> result =
                payoutService.getPayoutsByMilestoneId(10L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(payoutRepository)
                .findAllByMilestone_Id(10L);
    }


    /*
     * Helper methods
     */

    private CreatePayoutRequest createPayoutRequest() {
        CreatePayoutRequest request = new CreatePayoutRequest();
        request.setMilestoneId(10L);

        return request;
    }


    private User createUser(Long id, Role role) {
        User user = new User();
        user.setId(id);
        user.setFullName("Test User");
        user.setRole(role);

        return user;
    }


    private Campaign createCampaign() {
        User organization =
                createUser(50L, Role.ORGANIZATION);

        return Campaign.builder()
                .id(1L)
                .title("Food Campaign")
                .organization(organization)
                .currentAmount(new BigDecimal("5000.00"))
                .targetAmount(new BigDecimal("5000.00"))
                .build();
    }


    private Milestone createMilestone(
            Campaign campaign,
            MilestoneStatus status
    ) {
        return Milestone.builder()
                .id(10L)
                .campaign(campaign)
                .title("First Milestone")
                .description("First milestone description")
                .amount(new BigDecimal("1000.00"))
                .status(status)
                .createdAt(LocalDateTime.now().minusDays(2))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .build();
    }


    private Payout createExistingPayout(
            Long id,
            Campaign campaign,
            BigDecimal amount
    ) {
        Milestone previousMilestone =
                createMilestone(
                        campaign,
                        MilestoneStatus.COMPLETED
                );

        previousMilestone.setId(id);

        return Payout.builder()
                .id(id)
                .milestone(previousMilestone)
                .amount(amount)
                .status(PayoutStatus.EXECUTED)
                .createdAt(LocalDateTime.now().minusDays(1))
                .executedAt(LocalDateTime.now().minusDays(1))
                .createdBy(99L)
                .build();
    }
}