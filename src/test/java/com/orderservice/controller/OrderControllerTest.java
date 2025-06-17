package com.orderservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderservice.dto.OrderItemRequestDTO;
import com.orderservice.dto.OrderItemResponseDTO;
import com.orderservice.dto.OrderRequestDTO;
import com.orderservice.dto.OrderResponseDTO;
import com.orderservice.service.OrderService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void orderCreate_shouldReturn201() throws Exception {
        OrderItemRequestDTO itemRequestDTO = new OrderItemRequestDTO(UUID.randomUUID(), 1, new BigDecimal("150.00"));
        List<OrderItemRequestDTO> orderItemRequestList = new ArrayList<OrderItemRequestDTO>();
        orderItemRequestList.add(itemRequestDTO);
        OrderRequestDTO requestDTO = new OrderRequestDTO(UUID.randomUUID(), orderItemRequestList);

        OrderItemResponseDTO itemResponseDTO = new OrderItemResponseDTO(UUID.randomUUID(),UUID.randomUUID(), 1, new BigDecimal("150.00"));
        List<OrderItemResponseDTO> orderItemResponseList = new ArrayList<OrderItemResponseDTO>();
        orderItemResponseList.add(itemResponseDTO);
        OrderResponseDTO responseDTO = new OrderResponseDTO(UUID.randomUUID(), UUID.randomUUID(), orderItemResponseList, new BigDecimal("150.00"), new LocalDate.of(2025, 10, 10), new LocalDate.of(2025, 10, 10));


        Mockito.when(orderService.createOrder(Mockito.any())).thenReturn(responseDTO);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(responseDTO.id().toString()))
                .andExpect(jsonPath("$.status").value("PENDENTE"));
    }

    @Test
    void getById_shouldReturnOrder() throws Exception {
        UUID orderId = UUID.randomUUID();
        OrderItemResponseDTO itemResponseDTO = new OrderItemResponseDTO(UUID.randomUUID(),UUID.randomUUID(), 1, new BigDecimal("150.00"));
        List<OrderItemResponseDTO> orderItemResponseList = new ArrayList<OrderItemResponseDTO>();
        orderItemResponseList.add(itemResponseDTO);
        OrderResponseDTO responseDTO = new OrderResponseDTO(orderId, UUID.randomUUID(), orderItemResponseList, new BigDecimal("150.00"), new LocalDate.of(2025, 10, 10), new LocalDate.of(2025, 10, 10));

        Mockito.when(orderService.getById(orderId)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId.toString()))
                .andExpect(jsonPath("$.status").value("PENDENTE"));
    }

}
