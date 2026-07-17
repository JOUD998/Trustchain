package com.joud.trustchain.campaign;

import com.joud.trustchain.blockchain_transaction.BlockchainTransactionService;
import com.joud.trustchain.blockchain_transaction.BlockchainTransactionType;
import com.joud.trustchain.blockchain_transaction.BlockchainEntityType;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import com.joud.trustchain.campaign.dto.CampaignResponse;
import com.joud.trustchain.campaign.dto.CreateCampaignRequest;
import com.joud.trustchain.campaign.dto.UpdateCampaignRequest;
import com.joud.trustchain.security.CurrentUserService;
import com.joud.trustchain.user.Role;
import com.joud.trustchain.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CampaignServiceTest {

    @Mock
    private CampaignRepository campaignRepository;

    @Mock
    private BlockchainTransactionService blockchainTransactionService;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private CampaignService campaignService;

    @Test
    void shouldReturnCampaignWhenCampaignExists() {

        // Arrange
        Long campaignId = 1L;

        Campaign campaign = Campaign.builder()
                .id(campaignId)
                .title("Help Children")
                .build();

        when(campaignRepository.findById(campaignId))
                .thenReturn(Optional.of(campaign));

        // Act
        Campaign result = campaignService.findCampaignEntityById(campaignId);

        // Assert
        assertSame(campaign, result);
        assertEquals(campaignId, result.getId());
        assertEquals("Help Children", result.getTitle());

        verify(campaignRepository).findById(campaignId);
    }

    @Test
    void shouldThrowExceptionWhenCampaignDoesNotExist() {

        // Arrange
        Long campaignId = 99L;

        when(campaignRepository.findById(campaignId))
                .thenReturn(Optional.empty());

        // Act
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> campaignService.findCampaignEntityById(campaignId)
        );

        // Assert
        assertEquals(
                "Campaign not found with id: 99",
                exception.getMessage()
        );

        verify(campaignRepository).findById(campaignId);
    }

    @Test
    void shouldCreateCampaignSuccessfully() {

        // Arrange
        CreateCampaignRequest request = new CreateCampaignRequest();
        request.setTitle("Help Children");
        request.setDescription("Support children in need");
        request.setTargetAmount(new BigDecimal("10000"));

        User currentUser = new User();
        currentUser.setId(5L);
        currentUser.setFullName("Test Organization");
        currentUser.setRole(Role.ORGANIZATION);

        when(currentUserService.getCurrentUser())
                .thenReturn(currentUser);

        when(campaignRepository.save(any(Campaign.class)))
                .thenAnswer(invocation -> {
                    Campaign campaign = invocation.getArgument(0);
                    campaign.setId(1L);
                    return campaign;
                });

        // Act
        CampaignResponse result = campaignService.createCampaign(request);

        // Assert
        assertEquals(1L, result.getId());
        assertEquals("Help Children", result.getTitle());
        assertEquals("Support children in need", result.getDescription());
        assertEquals(new BigDecimal("10000"), result.getTargetAmount());
        assertEquals(BigDecimal.ZERO, result.getCurrentAmount());
        assertEquals(CampaignStatus.ACTIVE, result.getStatus());
        assertEquals(5L, result.getOrganizationId());
        assertEquals("Test Organization", result.getOrganizationName());

        verify(currentUserService).getCurrentUser();
        verify(campaignRepository).save(any(Campaign.class));

        verify(blockchainTransactionService).recordTransaction(
                BlockchainTransactionType.CAMPAIGN_CREATED,
                BlockchainEntityType.CAMPAIGN,
                1L,
                "Campaign 1 was created",
                5L
        );
    }

    @Test
    void shouldUpdateCampaignSuccessfully() {

        // Arrange
        Long campaignId = 1L;

        User currentUser = new User();
        currentUser.setId(5L);
        currentUser.setFullName("Test Organization");
        currentUser.setRole(Role.ORGANIZATION);

        Campaign campaign = Campaign.builder()
                .id(campaignId)
                .title("Old Title")
                .description("Old Description")
                .targetAmount(new BigDecimal("5000"))
                .currentAmount(BigDecimal.ZERO)
                .status(CampaignStatus.ACTIVE)
                .organization(currentUser)
                .build();

        UpdateCampaignRequest request = new UpdateCampaignRequest();
        request.setTitle("New Title");
        request.setDescription("New Description");
        request.setTargetAmount(new BigDecimal("10000"));

        when(currentUserService.getCurrentUser())
                .thenReturn(currentUser);

        when(campaignRepository.findById(campaignId))
                .thenReturn(Optional.of(campaign));

        when(campaignRepository.save(campaign))
                .thenReturn(campaign);

        // Act
        CampaignResponse result =
                campaignService.updateCampaign(campaignId, request);

        // Assert
        assertEquals("New Title", result.getTitle());
        assertEquals("New Description", result.getDescription());
        assertEquals(new BigDecimal("10000"), result.getTargetAmount());

        verify(currentUserService).getCurrentUser();
        verify(campaignRepository).findById(campaignId);
        verify(campaignRepository).save(campaign);

        verify(blockchainTransactionService).recordTransaction(
                BlockchainTransactionType.CAMPAIGN_UPDATED,
                BlockchainEntityType.CAMPAIGN,
                campaignId,
                "Campaign 1 was updated",
                5L
        );
    }

    @Test
    void shouldThrowExceptionWhenUserDoesNotOwnCampaign() {

        // Arrange
        Long campaignId = 1L;

        User owner = new User();
        owner.setId(5L);
        owner.setRole(Role.ORGANIZATION);

        User anotherUser = new User();
        anotherUser.setId(10L);
        anotherUser.setRole(Role.ORGANIZATION);

        Campaign campaign = Campaign.builder()
                .id(campaignId)
                .title("Test Campaign")
                .description("Test Description")
                .targetAmount(new BigDecimal("5000"))
                .currentAmount(BigDecimal.ZERO)
                .status(CampaignStatus.ACTIVE)
                .organization(owner)
                .build();

        UpdateCampaignRequest request = new UpdateCampaignRequest();
        request.setTitle("New Title");
        request.setDescription("New Description");
        request.setTargetAmount(new BigDecimal("10000"));

        when(currentUserService.getCurrentUser())
                .thenReturn(anotherUser);

        when(campaignRepository.findById(campaignId))
                .thenReturn(Optional.of(campaign));

        // Act + Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> campaignService.updateCampaign(campaignId, request)
        );

        assertEquals(
                "You are not authorized to manage this campaign",
                exception.getMessage()
        );

        verify(currentUserService).getCurrentUser();
        verify(campaignRepository).findById(campaignId);

        verify(campaignRepository, never()).save(any(Campaign.class));
        verify(blockchainTransactionService, never())
                .recordTransaction(
                        any(),
                        any(),
                        any(),
                        any(),
                        any()
                );
    }

    @Test
    void shouldThrowExceptionWhenCampaignNotFoundDuringUpdate() {

        // Arrange
        Long campaignId = 99L;

        UpdateCampaignRequest request = new UpdateCampaignRequest();
        request.setTitle("New Title");
        request.setDescription("New Description");
        request.setTargetAmount(new BigDecimal("10000"));

        when(campaignRepository.findById(campaignId))
                .thenReturn(Optional.empty());

        // Act + Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> campaignService.updateCampaign(campaignId, request)
        );

        assertNotNull(exception.getMessage());

        verify(campaignRepository).findById(campaignId);
        verify(campaignRepository, never()).save(any(Campaign.class));

        verify(currentUserService).getCurrentUser();
        verify(blockchainTransactionService, never())
                .recordTransaction(
                        any(),
                        any(),
                        any(),
                        any(),
                        any()
                );
    }

}