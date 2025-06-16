package com.orderservice.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PartnerResponseDTO(
        UUID id,
        String name,
        BigDecimal creditLimit
) {
}
