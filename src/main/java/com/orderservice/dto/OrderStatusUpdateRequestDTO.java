package com.orderservice.dto;

import com.orderservice.entity.Order;
import jakarta.validation.constraints.NotNull;

public record OrderStatusUpdateRequestDTO(
        @NotNull Order.OrderStatus status
) {
}
