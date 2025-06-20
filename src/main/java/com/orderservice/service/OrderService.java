package com.orderservice.service;

import com.orderservice.dto.*;
import com.orderservice.entity.Order;
import com.orderservice.entity.OrderItem;
import com.orderservice.entity.Partner;
import com.orderservice.event.OrderStatusEventPublisher;
import com.orderservice.exception.InsufficientCreditException;
import com.orderservice.exception.OrderNotFoundException;
import com.orderservice.exception.PartnerNotFoundException;
import com.orderservice.repository.OrderItemRepository;
import com.orderservice.repository.OrderRepository;
import com.orderservice.repository.PartnerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final String PARTNER_NOT_FOUND = "Partner not found!";
    private static final String PARTNER_INSUFICIENT_CREDIT = "Partner does not have sufficient credit!";
    private static final String ORDER_NOT_FOUND = "Order not found!";
    private static final String ADD = "add";
    private static final String SUB = "sub";
    private static final int ZERO = 0;

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PartnerRepository partnerRepository;
    private final OrderStatusEventPublisher eventPublisher;

    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO dto) {
        Partner partner = partnerRepository.findById(dto.partnerId())
                .orElseThrow(() -> new PartnerNotFoundException(PARTNER_NOT_FOUND));

        BigDecimal totalAmount = getTotalAmount(dto.items());

        if (partner.getCreditLimit().compareTo(totalAmount) < ZERO) {
            throw new InsufficientCreditException(PARTNER_INSUFICIENT_CREDIT);
        }

        Order order = Order.builder()
                .partnerId(dto.partnerId())
                .status(Order.OrderStatus.PENDENTE)
                .totalAmount(totalAmount)
                .build();

        order = orderRepository.save(order);
        Order finalOrder = order;
        List<OrderItem> items = dto.items().stream().map(item ->
                OrderItem.builder()
                        .order(finalOrder)
                        .productId(item.productId())
                        .quantity(item.quantity())
                        .unitPrice(item.unitPrice())
                        .build()
        ).toList();

        orderItemRepository.saveAll(items);
        order.setItems(items);

        return toResponseDTO(order);
    }

    public OrderResponseDTO getById(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(ORDER_NOT_FOUND));
        return toResponseDTO(order);
    }

    public List<OrderResponseDTO> getByStatus(Order.OrderStatus status) {
        return orderRepository.findByStatus(status).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public List<OrderResponseDTO> getByPeriod(LocalDateTime start, LocalDateTime end) {
        return orderRepository.findByCreatedAtBetween(start, end).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional
    public OrderResponseDTO updateStatus(UUID id, OrderStatusUpdateRequestDTO dto) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(ORDER_NOT_FOUND));

        Order.OrderStatus previousStatus = order.getStatus();
        order.setStatus(dto.status());
        order = orderRepository.save(order);

        //TODO create a logical flow for order status doesn't return
        if (previousStatus == Order.OrderStatus.PENDENTE &&
                dto.status() == Order.OrderStatus.APROVADO) {
            creditChange(order, SUB);
        }

        eventPublisher.publishStatusChange(order);

        return toResponseDTO(order);
    }

    @Transactional
    public void orderCancel(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(ORDER_NOT_FOUND));

        //TODO verify if order already canceled before change status
        order.setStatus(Order.OrderStatus.CANCELADO);
        orderRepository.save(order);

        creditChange(order, ADD);

        eventPublisher.publishStatusChange(order);
    }

    private void creditChange(Order order, String operation) {
        Partner partner = partnerRepository.findById(order.getPartnerId())
                .orElseThrow(() -> new PartnerNotFoundException(PARTNER_NOT_FOUND));

        if (SUB.equals(operation) && partner.getCreditLimit().compareTo(order.getTotalAmount()) < ZERO) {
            throw new InsufficientCreditException(PARTNER_INSUFICIENT_CREDIT);
        }

        if(SUB.equals(operation)) {
            partner.setCreditLimit(partner.getCreditLimit().subtract(order.getTotalAmount()));
        } else {
            partner.setCreditLimit(partner.getCreditLimit().add(order.getTotalAmount()));
        }

        partnerRepository.save(partner);
    }

    private BigDecimal getTotalAmount(List<OrderItemRequestDTO> items) {
        return items.stream()
                .map(item -> item.unitPrice().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private OrderResponseDTO toResponseDTO(Order order) {
        List<OrderItemResponseDTO> items = order.getItems().stream().map(item ->
                new OrderItemResponseDTO(
                        item.getId(),
                        item.getProductId(),
                        item.getQuantity(),
                        item.getUnitPrice()
                )
        ).toList();

        return new OrderResponseDTO(
                order.getId(),
                order.getPartnerId(),
                items,
                order.getTotalAmount(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

}