package com.financepro.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * User — a registered Finance Pro account. Stored as a plain POJO inside
 * project.json (no JPA / database).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User {

    private Long id;

    @NotBlank
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

    private String fullName;

    @Builder.Default
    private String currency = "INR";

    @Builder.Default
    private Boolean darkMode = false;

    @Builder.Default
    private String role = "USER";

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
