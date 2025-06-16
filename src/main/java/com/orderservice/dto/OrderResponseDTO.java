package com.orderservice.dto;

import com.orderservice.entity.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponseDTO(
        UUID id,
        UUID partnerId,
        List<OrderItemResponseDTO> items,
        BigDecimal totalAmount,
        Order.OrderStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
