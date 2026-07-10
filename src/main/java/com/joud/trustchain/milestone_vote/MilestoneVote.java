package com.joud.trustchain.milestone_vote;

import com.joud.trustchain.milestone.Milestone;
import com.joud.trustchain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "milestone_vote",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"milestone_id", "voter_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MilestoneVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "milestone_id", nullable = false)
    private Milestone milestone;

    @ManyToOne
    @JoinColumn(name = "voter_id", nullable = false)
    private User voter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MilestoneVoteDecision decision;

    private String comment;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}