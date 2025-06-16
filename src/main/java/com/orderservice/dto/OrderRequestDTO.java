package com.orderservice.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record OrderRequestDTO(
        @NotNull UUID partnerId,
        @NotEmpty List<OrderItemRequestDTO> items
) {
}
