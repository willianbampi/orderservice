package com.orderservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PartnerRequestDTO(
        @NotBlank String name,
        @NotNull @Min(0) BigDecimal creditLimit
) {
}
