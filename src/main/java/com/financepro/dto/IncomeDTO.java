package com.financepro.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Data Transfer Object for Income create / update operations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncomeDTO {

    private Long id;

    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must be at most 100 characters")
    private String title;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotBlank(message = "Source is required")
    private String source;

    @NotNull(message = "Date is required")
    private LocalDate incomeDate;

    private String notes;
}
