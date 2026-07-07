package com.financepro.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for SavingsGoal read operations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavingsGoalDTO {

    private Long id;
    private String title;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private LocalDate deadline;
    private int progressPercentage;
}
