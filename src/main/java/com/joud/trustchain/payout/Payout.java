package com.joud.trustchain.payout;

import com.joud.trustchain.milestone.Milestone;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payout")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne

    @JoinColumn(

            name = "milestone_id",

            nullable = false,

            unique = true

    )
    private Milestone milestone;

    @NotNull
    @Positive
    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PayoutStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime executedAt;

    @Column(nullable = false)
    private Long createdBy;
}