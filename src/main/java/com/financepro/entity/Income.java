package com.financepro.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Income — a single income record. Stored in project.json.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Income {

    private Long id;

    @NotBlank
    @Size(max = 100)
    private String title;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;

    @NotBlank
    private String source;

    @NotNull
    private LocalDate incomeDate;

    private String notes;

    private Long userId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
