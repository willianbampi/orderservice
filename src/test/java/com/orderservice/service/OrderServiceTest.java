package com.orderservice.service;

import com.orderservice.dto.OrderItemRequestDTO;
import com.orderservice.dto.OrderRequestDTO;
import com.orderservice.dto.OrderResponseDTO;
import com.orderservice.entity.Order;
import com.orderservice.entity.Partner;
import com.orderservice.exception.InsuficientCreditException;
import com.orderservice.repository.OrderRepository;
import com.orderservice.repository.PartnerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PartnerRepository partnerRepository;

    @InjectMocks
    private OrderService orderService;

    private Partner partner;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        partner = new Partner();
        partner.setId(UUID.randomUUID());
        partner.setName("Parceiro A");
        partner.setCreditLimit(new BigDecimal("1000.00"));
    }

    @Test
    void createOrder_withSuficientCredit_shouldSaveOrder() {
        OrderItemRequestDTO item = new OrderItemRequestDTO(UUID.randomUUID(), 1, new BigDecimal("150.00"));
        List<OrderItemRequestDTO> orderItemList = new ArrayList<OrderItemRequestDTO>();
        orderItemList.add(item);
        OrderRequestDTO orderDTO = new OrderRequestDTO(UUID.randomUUID(), orderItemList);

        when(partnerRepository.findById(partner.getId())).thenReturn(Optional.of(partner));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        OrderResponseDTO result = orderService.createOrder(orderDTO);

        assertNotNull(result);
        Order order = Order.builder()
                .partnerId(orderDTO.partnerId())
                .status(Order.OrderStatus.PENDENTE)
                .totalAmount(result.totalAmount())
                .build();
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void createOrder_withInsuficientCredit_shouldThrowsException() {
        OrderItemRequestDTO item = new OrderItemRequestDTO(UUID.randomUUID(), 1, new BigDecimal("2000.00"));
        List<OrderItemRequestDTO> orderItemList = new ArrayList<OrderItemRequestDTO>();
        orderItemList.add(item);
        OrderRequestDTO orderDTO = new OrderRequestDTO(UUID.randomUUID(), orderItemList);

        when(partnerRepository.findById(partner.getId())).thenReturn(Optional.of(partner));

        assertThrows(InsuficientCreditException.class, () -> orderService.createOrder(orderDTO));
        verify(orderRepository, never()).save(any());
    }

}
