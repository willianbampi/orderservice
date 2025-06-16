package com.orderservice.event;

import com.orderservice.entity.Order;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrderStatusEvent(
        UUID orderId,
        UUID partnerId,
        BigDecimal totalAmount,
        Order.OrderStatus status,
        LocalDateTime updatedAt
) implements Serializable {
}
