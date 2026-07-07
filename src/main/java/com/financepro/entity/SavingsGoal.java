package com.financepro.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * SavingsGoal — a target the user is saving toward. Stored in project.json.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SavingsGoal {

    private Long id;

    @NotBlank
    @Size(max = 100)
    private String title;

    @NotNull
    @DecimalMin("1.00")
    private BigDecimal targetAmount;

    @Builder.Default
    private BigDecimal currentAmount = BigDecimal.ZERO;

    private LocalDate deadline;

    private Long userId;

    private LocalDateTime createdAt;

    /** Returns the completion ratio (0..100) — not persisted, derived. */
    public int getProgressPercentage() {
        if (targetAmount == null || targetAmount.compareTo(BigDecimal.ZERO) == 0) return 0;
        if (currentAmount == null) return 0;
        BigDecimal pct = currentAmount
                .divide(targetAmount, 2, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
        return Math.min(pct.intValue(), 100);
    }
}
