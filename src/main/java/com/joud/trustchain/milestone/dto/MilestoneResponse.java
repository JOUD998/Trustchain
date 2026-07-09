package com.joud.trustchain.milestone.dto;
import com.joud.trustchain.milestone.MilestoneStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MilestoneResponse {

    private Long id;

    private Long campaignId;
    private String campaignTitle;


    private String title;

    private String description;

    private BigDecimal amount;

    private MilestoneStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;


}
