package com.joud.trustchain.milestone_vote;

import com.joud.trustchain.blockchain_transaction.BlockchainEntityType;
import com.joud.trustchain.blockchain_transaction.BlockchainTransactionService;
import com.joud.trustchain.blockchain_transaction.BlockchainTransactionType;
import com.joud.trustchain.campaign.Campaign;
import com.joud.trustchain.milestone.Milestone;
import com.joud.trustchain.milestone.MilestoneService;
import com.joud.trustchain.milestone.MilestoneStatus;
import com.joud.trustchain.milestone_vote.dto.CreateMilestoneVoteRequest;
import com.joud.trustchain.milestone_vote.dto.MilestoneVoteResponse;
import com.joud.trustchain.security.CurrentUserService;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MilestoneVoteServiceTest {

    @Mock
    private MilestoneVoteRepository milestoneVoteRepository;

    @Mock
    private MilestoneService milestoneService;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private BlockchainTransactionService blockchainTransactionService;

    @InjectMocks
    private MilestoneVoteService milestoneVoteService;


    @Test
    void shouldCreateVoteSuccessfully() {

        // Arrange
        User voter = createVoter();
        Milestone milestone = createMilestone(MilestoneStatus.PENDING);

        CreateMilestoneVoteRequest request =
                createVoteRequest(
                        MilestoneVoteDecision.APPROVE,
                        "Everything looks correct"
                );

        when(currentUserService.getCurrentUser())
                .thenReturn(voter);

        when(milestoneVoteRepository
                .existsByMilestone_IdAndVoter_Id(10L, 5L))
                .thenReturn(false);

        when(milestoneService.findMilestoneEntityById(10L))
                .thenReturn(milestone);

        when(milestoneVoteRepository.save(any(MilestoneVote.class)))
                .thenAnswer(invocation -> {
                    MilestoneVote vote = invocation.getArgument(0);
                    vote.setId(100L);
                    return vote;
                });

        /*
         * Not enough approve votes yet,
         * so milestone remains pending.
         */
        when(milestoneVoteRepository
                .countByMilestone_IdAndDecision(
                        10L,
                        MilestoneVoteDecision.APPROVE
                ))
                .thenReturn(1L);

        when(milestoneVoteRepository
                .countByMilestone_IdAndDecision(
                        10L,
                        MilestoneVoteDecision.REJECT
                ))
                .thenReturn(0L);

        // Act
        MilestoneVoteResponse result =
                milestoneVoteService.createVote(10L, request);

        // Assert: response
        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(10L, result.getMilestoneId());
        assertEquals("First Milestone", result.getMilestoneTitle());
        assertEquals(5L, result.getVoterId());
        assertEquals("Test Validator", result.getVoterFullName());
        assertEquals(
                MilestoneVoteDecision.APPROVE,
                result.getDecision()
        );
        assertEquals(
                "Everything looks correct",
                result.getComment()
        );
        assertNotNull(result.getCreatedAt());

        // Milestone must remain pending
        assertEquals(
                MilestoneStatus.PENDING,
                milestone.getStatus()
        );

        // Capture saved vote
        ArgumentCaptor<MilestoneVote> voteCaptor =
                ArgumentCaptor.forClass(MilestoneVote.class);

        verify(milestoneVoteRepository)
                .save(voteCaptor.capture());

        MilestoneVote savedVote = voteCaptor.getValue();

        assertEquals(milestone, savedVote.getMilestone());
        assertEquals(voter, savedVote.getVoter());
        assertEquals(
                MilestoneVoteDecision.APPROVE,
                savedVote.getDecision()
        );
        assertEquals(
                "Everything looks correct",
                savedVote.getComment()
        );
        assertNotNull(savedVote.getCreatedAt());

        verify(blockchainTransactionService)
                .recordTransaction(
                        BlockchainTransactionType.MILESTONE_VOTE_CAST,
                        BlockchainEntityType.MILESTONE_VOTE,
                        100L,
                        "User 5 voted APPROVE on milestone 10",
                        5L
                );
    }


    @Test
    void shouldThrowExceptionWhenUserAlreadyVoted() {

        // Arrange
        User voter = createVoter();

        CreateMilestoneVoteRequest request =
                createVoteRequest(
                        MilestoneVoteDecision.APPROVE,
                        "Approved"
                );

        when(currentUserService.getCurrentUser())
                .thenReturn(voter);

        when(milestoneVoteRepository
                .existsByMilestone_IdAndVoter_Id(10L, 5L))
                .thenReturn(true);

        // Act + Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> milestoneVoteService.createVote(10L, request)
        );

        assertEquals(
                "You have already voted for this milestone",
                exception.getMessage()
        );

        verify(currentUserService).getCurrentUser();

        verify(milestoneVoteRepository)
                .existsByMilestone_IdAndVoter_Id(10L, 5L);

        verifyNoInteractions(milestoneService);

        verify(milestoneVoteRepository, never())
                .save(any(MilestoneVote.class));

        verifyNoInteractions(blockchainTransactionService);
    }


    @Test
    void shouldApproveMilestoneWhenEnoughApproveVotesExist() {

        // Arrange
        User voter = createVoter();
        Milestone milestone = createMilestone(MilestoneStatus.PENDING);

        CreateMilestoneVoteRequest request =
                createVoteRequest(
                        MilestoneVoteDecision.APPROVE,
                        "Second approval"
                );

        when(currentUserService.getCurrentUser())
                .thenReturn(voter);

        when(milestoneVoteRepository
                .existsByMilestone_IdAndVoter_Id(10L, 5L))
                .thenReturn(false);

        when(milestoneService.findMilestoneEntityById(10L))
                .thenReturn(milestone);

        when(milestoneVoteRepository.save(any(MilestoneVote.class)))
                .thenAnswer(invocation -> {
                    MilestoneVote vote = invocation.getArgument(0);
                    vote.setId(100L);
                    return vote;
                });

        when(milestoneVoteRepository
                .countByMilestone_IdAndDecision(
                        10L,
                        MilestoneVoteDecision.APPROVE
                ))
                .thenReturn(2L);

        when(milestoneVoteRepository
                .countByMilestone_IdAndDecision(
                        10L,
                        MilestoneVoteDecision.REJECT
                ))
                .thenReturn(0L);

        // Act
        MilestoneVoteResponse result =
                milestoneVoteService.createVote(10L, request);

        // Assert
        assertNotNull(result);

        assertEquals(
                MilestoneStatus.APPROVED,
                milestone.getStatus()
        );

        assertNotNull(milestone.getUpdatedAt());

        /*
         * One blockchain transaction for the vote,
         * and one for milestone approval.
         */
        verify(blockchainTransactionService)
                .recordTransaction(
                        BlockchainTransactionType.MILESTONE_VOTE_CAST,
                        BlockchainEntityType.MILESTONE_VOTE,
                        100L,
                        "User 5 voted APPROVE on milestone 10",
                        5L
                );

        verify(blockchainTransactionService)
                .recordTransaction(
                        BlockchainTransactionType.MILESTONE_APPROVED,
                        BlockchainEntityType.MILESTONE,
                        10L,
                        "Milestone 'First Milestone' was approved with "
                                + "2 approve votes and 0 reject votes",
                        5L
                );

        verify(blockchainTransactionService, times(2))
                .recordTransaction(
                        any(),
                        any(),
                        any(),
                        anyString(),
                        any()
                );
    }


    @Test
    void shouldNotApproveMilestoneWhenApproveVotesAreLessThanTwo() {

        // Arrange
        User voter = createVoter();
        Milestone milestone = createMilestone(MilestoneStatus.PENDING);

        CreateMilestoneVoteRequest request =
                createVoteRequest(
                        MilestoneVoteDecision.APPROVE,
                        "First approval"
                );

        mockSuccessfulVoteCreation(voter, milestone);

        when(milestoneVoteRepository
                .countByMilestone_IdAndDecision(
                        10L,
                        MilestoneVoteDecision.APPROVE
                ))
                .thenReturn(1L);

        when(milestoneVoteRepository
                .countByMilestone_IdAndDecision(
                        10L,
                        MilestoneVoteDecision.REJECT
                ))
                .thenReturn(0L);

        // Act
        milestoneVoteService.createVote(10L, request);

        // Assert
        assertEquals(
                MilestoneStatus.PENDING,
                milestone.getStatus()
        );

        verify(blockchainTransactionService, times(1))
                .recordTransaction(
                        any(),
                        any(),
                        any(),
                        anyString(),
                        any()
                );

        verify(blockchainTransactionService, never())
                .recordTransaction(
                        eq(BlockchainTransactionType.MILESTONE_APPROVED),
                        eq(BlockchainEntityType.MILESTONE),
                        any(),
                        anyString(),
                        any()
                );
    }


    @Test
    void shouldNotApproveMilestoneWhenRejectVotesAreEqualToApproveVotes() {

        // Arrange
        User voter = createVoter();
        Milestone milestone = createMilestone(MilestoneStatus.PENDING);

        CreateMilestoneVoteRequest request =
                createVoteRequest(
                        MilestoneVoteDecision.APPROVE,
                        "Approve vote"
                );

        mockSuccessfulVoteCreation(voter, milestone);

        when(milestoneVoteRepository
                .countByMilestone_IdAndDecision(
                        10L,
                        MilestoneVoteDecision.APPROVE
                ))
                .thenReturn(2L);

        when(milestoneVoteRepository
                .countByMilestone_IdAndDecision(
                        10L,
                        MilestoneVoteDecision.REJECT
                ))
                .thenReturn(2L);

        // Act
        milestoneVoteService.createVote(10L, request);

        // Assert
        assertEquals(
                MilestoneStatus.PENDING,
                milestone.getStatus()
        );

        verify(blockchainTransactionService, times(1))
                .recordTransaction(
                        any(),
                        any(),
                        any(),
                        anyString(),
                        any()
                );

        verify(blockchainTransactionService, never())
                .recordTransaction(
                        eq(BlockchainTransactionType.MILESTONE_APPROVED),
                        eq(BlockchainEntityType.MILESTONE),
                        any(),
                        anyString(),
                        any()
                );
    }


    @Test
    void shouldNotApproveMilestoneWhenRejectVotesAreGreaterThanApproveVotes() {

        // Arrange
        User voter = createVoter();
        Milestone milestone = createMilestone(MilestoneStatus.PENDING);

        CreateMilestoneVoteRequest request =
                createVoteRequest(
                        MilestoneVoteDecision.REJECT,
                        "Not acceptable"
                );

        mockSuccessfulVoteCreation(voter, milestone);

        when(milestoneVoteRepository
                .countByMilestone_IdAndDecision(
                        10L,
                        MilestoneVoteDecision.APPROVE
                ))
                .thenReturn(2L);

        when(milestoneVoteRepository
                .countByMilestone_IdAndDecision(
                        10L,
                        MilestoneVoteDecision.REJECT
                ))
                .thenReturn(3L);

        // Act
        milestoneVoteService.createVote(10L, request);

        // Assert
        assertEquals(
                MilestoneStatus.PENDING,
                milestone.getStatus()
        );

        verify(blockchainTransactionService, times(1))
                .recordTransaction(
                        any(),
                        any(),
                        any(),
                        anyString(),
                        any()
                );

        verify(blockchainTransactionService, never())
                .recordTransaction(
                        eq(BlockchainTransactionType.MILESTONE_APPROVED),
                        eq(BlockchainEntityType.MILESTONE),
                        any(),
                        anyString(),
                        any()
                );
    }


    @Test
    void shouldNotReapproveAlreadyApprovedMilestone() {

        // Arrange
        User voter = createVoter();
        Milestone milestone = createMilestone(MilestoneStatus.APPROVED);

        LocalDateTime originalUpdatedAt =
                LocalDateTime.now().minusDays(1);

        milestone.setUpdatedAt(originalUpdatedAt);

        CreateMilestoneVoteRequest request =
                createVoteRequest(
                        MilestoneVoteDecision.APPROVE,
                        "Additional vote"
                );

        when(currentUserService.getCurrentUser())
                .thenReturn(voter);

        when(milestoneVoteRepository
                .existsByMilestone_IdAndVoter_Id(10L, 5L))
                .thenReturn(false);

        when(milestoneService.findMilestoneEntityById(10L))
                .thenReturn(milestone);

        when(milestoneVoteRepository.save(any(MilestoneVote.class)))
                .thenAnswer(invocation -> {
                    MilestoneVote vote = invocation.getArgument(0);
                    vote.setId(100L);
                    return vote;
                });

        // Act
        milestoneVoteService.createVote(10L, request);

        // Assert
        assertEquals(
                MilestoneStatus.APPROVED,
                milestone.getStatus()
        );

        /*
         * Because method returns immediately,
         * updatedAt must not change.
         */
        assertEquals(
                originalUpdatedAt,
                milestone.getUpdatedAt()
        );

        verify(
                milestoneVoteRepository,
                never()
        ).countByMilestone_IdAndDecision(
                anyLong(),
                any(MilestoneVoteDecision.class)
        );

        verify(blockchainTransactionService, times(1))
                .recordTransaction(
                        eq(BlockchainTransactionType.MILESTONE_VOTE_CAST),
                        eq(BlockchainEntityType.MILESTONE_VOTE),
                        eq(100L),
                        anyString(),
                        eq(5L)
                );

        verify(blockchainTransactionService, never())
                .recordTransaction(
                        eq(BlockchainTransactionType.MILESTONE_APPROVED),
                        eq(BlockchainEntityType.MILESTONE),
                        any(),
                        anyString(),
                        any()
                );
    }


    @Test
    void shouldReturnVotesByMilestoneId() {

        // Arrange
        Milestone milestone = createMilestone(MilestoneStatus.PENDING);

        User firstVoter = createVoter();

        User secondVoter = new User();
        secondVoter.setId(6L);
        secondVoter.setFullName("Second Validator");

        MilestoneVote firstVote = createVote(
                100L,
                milestone,
                firstVoter,
                MilestoneVoteDecision.APPROVE,
                "Approved"
        );

        MilestoneVote secondVote = createVote(
                101L,
                milestone,
                secondVoter,
                MilestoneVoteDecision.REJECT,
                "Rejected"
        );

        when(milestoneVoteRepository
                .findAllByMilestone_Id(10L))
                .thenReturn(List.of(firstVote, secondVote));

        // Act
        List<MilestoneVoteResponse> result =
                milestoneVoteService.getVotesByMilestoneId(10L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        MilestoneVoteResponse firstResult = result.get(0);
        MilestoneVoteResponse secondResult = result.get(1);

        assertEquals(100L, firstResult.getId());
        assertEquals(10L, firstResult.getMilestoneId());
        assertEquals("First Milestone", firstResult.getMilestoneTitle());
        assertEquals(5L, firstResult.getVoterId());
        assertEquals("Test Validator", firstResult.getVoterFullName());
        assertEquals(
                MilestoneVoteDecision.APPROVE,
                firstResult.getDecision()
        );
        assertEquals("Approved", firstResult.getComment());

        assertEquals(101L, secondResult.getId());
        assertEquals(6L, secondResult.getVoterId());
        assertEquals(
                "Second Validator",
                secondResult.getVoterFullName()
        );
        assertEquals(
                MilestoneVoteDecision.REJECT,
                secondResult.getDecision()
        );

        verify(milestoneVoteRepository)
                .findAllByMilestone_Id(10L);
    }


    @Test
    void shouldReturnEmptyListWhenNoVotesExist() {

        // Arrange
        when(milestoneVoteRepository
                .findAllByMilestone_Id(10L))
                .thenReturn(List.of());

        // Act
        List<MilestoneVoteResponse> result =
                milestoneVoteService.getVotesByMilestoneId(10L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(milestoneVoteRepository)
                .findAllByMilestone_Id(10L);
    }


    /*
     * Helper methods
     */

    private void mockSuccessfulVoteCreation(
            User voter,
            Milestone milestone
    ) {
        when(currentUserService.getCurrentUser())
                .thenReturn(voter);

        when(milestoneVoteRepository
                .existsByMilestone_IdAndVoter_Id(10L, 5L))
                .thenReturn(false);

        when(milestoneService.findMilestoneEntityById(10L))
                .thenReturn(milestone);

        when(milestoneVoteRepository.save(any(MilestoneVote.class)))
                .thenAnswer(invocation -> {
                    MilestoneVote vote = invocation.getArgument(0);
                    vote.setId(100L);
                    return vote;
                });
    }


    private User createVoter() {
        User voter = new User();
        voter.setId(5L);
        voter.setFullName("Test Validator");

        return voter;
    }


    private Campaign createCampaign() {
        return Campaign.builder()
                .id(1L)
                .title("Food Campaign")
                .build();
    }


    private Milestone createMilestone(
            MilestoneStatus status
    ) {
        return Milestone.builder()
                .id(10L)
                .campaign(createCampaign())
                .title("First Milestone")
                .description("First milestone description")
                .amount(new BigDecimal("1000.00"))
                .status(status)
                .createdAt(LocalDateTime.now().minusDays(2))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .build();
    }


    private CreateMilestoneVoteRequest createVoteRequest(
            MilestoneVoteDecision decision,
            String comment
    ) {
        CreateMilestoneVoteRequest request =
                new CreateMilestoneVoteRequest();

        request.setDecision(decision);
        request.setComment(comment);

        return request;
    }


    private MilestoneVote createVote(
            Long id,
            Milestone milestone,
            User voter,
            MilestoneVoteDecision decision,
            String comment
    ) {
        return MilestoneVote.builder()
                .id(id)
                .milestone(milestone)
                .voter(voter)
                .decision(decision)
                .comment(comment)
                .createdAt(LocalDateTime.now())
                .build();
    }
}