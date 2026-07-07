package com.financepro.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Transaction — unified record of an income or expense. Stored in project.json.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Transaction {

    private Long id;

    @NotBlank
    @Size(max = 100)
    private String title;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;

    /** "INCOME" or "EXPENSE" */
    @NotBlank
    private String type;

    private String category;

    @NotNull
    private LocalDate transactionDate;

    private String notes;

    private Long userId;

    private LocalDateTime createdAt;
}
