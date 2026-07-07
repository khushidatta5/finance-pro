package com.financepro.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for Transaction read operations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionDTO {

    private Long id;
    private String title;
    private BigDecimal amount;
    private String type;       // INCOME | EXPENSE
    private String category;
    private LocalDate transactionDate;
    private String notes;
    private LocalDateTime createdAt;
}
