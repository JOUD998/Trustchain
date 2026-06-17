package com.joud.trustchain.donation;


import com.joud.trustchain.campaign.Campaign;
import com.joud.trustchain.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "donation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Donation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    BigDecimal amount;

    @DateTimeFormat
    @Column(nullable = false)
    LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(nullable = false)
    User user;

    @ManyToOne
    @JoinColumn(nullable = false)
    Campaign campaign;
}
