package com.financepro.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Expense — a single spending record. Stored in project.json.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Expense {

    private Long id;

    @NotBlank
    @Size(max = 100)
    private String title;

    /** Always stored as a strictly positive value in rupees (₹). */
    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;

    @NotBlank
    private String category;

    @NotNull
    private LocalDate expenseDate;

    private String notes;

    /** Owning user — referenced by id only to keep JSON flat. */
    private Long userId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
