package com.joud.trustchain.voucher;
import com.joud.trustchain.campaign.Campaign;
import com.joud.trustchain.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "voucher")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VoucherStatus status;

    @Column(nullable = false)
    private LocalDateTime issuedAt;


    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private LocalDateTime redeemedAt;


    @ManyToOne
    @JoinColumn(nullable = false)
    private Campaign campaign;

    @ManyToOne
    @JoinColumn(nullable = false)
    private User beneficiary;

}
