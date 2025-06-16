package com.orderservice.controller;

import com.orderservice.dto.OrderRequestDTO;
import com.orderservice.dto.OrderResponseDTO;
import com.orderservice.dto.OrderStatusUpdateRequestDTO;
import com.orderservice.entity.Order;
import com.orderservice.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order Management")
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Create a new order")
    @PostMapping
    public ResponseEntity<OrderResponseDTO> create(@Valid @RequestBody OrderRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(dto));
    }

    @Operation(summary = "Get order by id")
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.getById(id));
    }

    @Operation(summary = "Get orders by status")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderResponseDTO>> getByStatus(@PathVariable Order.OrderStatus status) {
        return ResponseEntity.ok(orderService.getByStatus(status));
    }

    @Operation(summary = "Get an order by period")
    @GetMapping("/period")
    public ResponseEntity<List<OrderResponseDTO>> getByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        return ResponseEntity.ok(orderService.getByPeriod(start, end));
    }

    @Operation(summary = "Update order status")
    @PutMapping("/{id}/status")
    public ResponseEntity<OrderResponseDTO> statusUpdate(
            @PathVariable UUID id,
            @Valid @RequestBody OrderStatusUpdateRequestDTO dto
    ) {
        return ResponseEntity.ok(orderService.updateStatus(id, dto));
    }

    @Operation(summary = "Order cancel")
    @PutMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable UUID id) {
        orderService.orderCancel(id);
        return ResponseEntity.noContent().build();
    }

}
