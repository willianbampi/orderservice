package com.orderservice.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponseDTO(
        UUID id,
        UUID productId,
        Integer quantity,
        BigDecimal unitPrice
) {
}
