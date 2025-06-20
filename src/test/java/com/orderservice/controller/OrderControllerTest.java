package com.orderservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderservice.dto.*;
import com.orderservice.entity.Order;
import com.orderservice.entity.Partner;
import com.orderservice.exception.GlobalExceptionHandler;
import com.orderservice.exception.OrderNotFoundException;
import com.orderservice.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
public class OrderControllerTest {

    private static final UUID PARTNER_ID = UUID.randomUUID();
    private static final UUID PRODUCT_ID = UUID.randomUUID();
    private static final UUID ORDER_ITEM_ID = UUID.randomUUID();
    private static final UUID ORDER_ID = UUID.randomUUID();
    private static final String PARTNER_A_NAME = "Partner A";
    private static final BigDecimal CREDIT_LIMIT_INITIAL = new BigDecimal("1000.00");
    private static final BigDecimal ORDER_TOTAL_AMOUNT = new BigDecimal("500.00");
    private static final BigDecimal ORDER_ITEM_UNIT_PRICE = new BigDecimal("500.00");
    private static final BigDecimal ORDER_ITEM_UNIT_PRICE_NEW = new BigDecimal("1500.00");
    private static final Order.OrderStatus ORDER_STATUS_PENDENTE = Order.OrderStatus.PENDENTE;
    private static final Order.OrderStatus ORDER_STATUS_ENVIADO = Order.OrderStatus.ENVIADO;
    private static final LocalDateTime CREATED_AT = LocalDate.of(2020, Month.JANUARY, 18).atStartOfDay();
    private static final LocalDateTime UPDATED_AT = LocalDate.of(2020, Month.JANUARY, 18).atStartOfDay();
    private static final LocalDateTime START_DATE = LocalDate.of(2020, Month.JANUARY, 17).atStartOfDay();
    private static final LocalDateTime END_DATE = LocalDate.of(2020, Month.JANUARY, 19).atStartOfDay();
    private static final Partner PARTNER = new Partner(PARTNER_ID, PARTNER_A_NAME, CREDIT_LIMIT_INITIAL, CREATED_AT,
                                                       UPDATED_AT);
    private static final String ORDER_NOT_FOUND = "Order not found!";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        orderService = Mockito.mock(OrderService.class);
        objectMapper = new ObjectMapper();

        mockMvc = MockMvcBuilders
                .standaloneSetup(new OrderController(orderService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private OrderRequestDTO buildOrderRequestDTO() {
        OrderItemRequestDTO orderItemRequestDTO = new OrderItemRequestDTO(ORDER_ITEM_ID, 1, ORDER_ITEM_UNIT_PRICE);
        List<OrderItemRequestDTO> orderItemRequestDTOList = new ArrayList<OrderItemRequestDTO>();
        orderItemRequestDTOList.add(orderItemRequestDTO);
        OrderRequestDTO orderRequestDTO = new OrderRequestDTO(PARTNER_ID, orderItemRequestDTOList);
        return orderRequestDTO;
    }

    private OrderResponseDTO buildOrderResponseDTO() {
        OrderItemResponseDTO orderItemResponseDTO = new OrderItemResponseDTO(ORDER_ITEM_ID, PRODUCT_ID, 1, ORDER_ITEM_UNIT_PRICE);
        List<OrderItemResponseDTO> orderItemResponseDTOList = new ArrayList<OrderItemResponseDTO>();
        orderItemResponseDTOList.add(orderItemResponseDTO);
        OrderResponseDTO orderResponseDTO = new OrderResponseDTO(ORDER_ID, PARTNER_ID, orderItemResponseDTOList, ORDER_TOTAL_AMOUNT,
                                                                 ORDER_STATUS_PENDENTE, CREATED_AT, UPDATED_AT);
        return orderResponseDTO;
    }

    private OrderResponseDTO buildOrderEnviadoResponseDTO() {
        OrderItemResponseDTO orderItemResponseDTO = new OrderItemResponseDTO(ORDER_ITEM_ID, PRODUCT_ID, 1, ORDER_ITEM_UNIT_PRICE);
        List<OrderItemResponseDTO> orderItemResponseDTOList = new ArrayList<OrderItemResponseDTO>();
        orderItemResponseDTOList.add(orderItemResponseDTO);
        OrderResponseDTO orderResponseDTO = new OrderResponseDTO(ORDER_ID, PARTNER_ID, orderItemResponseDTOList, ORDER_TOTAL_AMOUNT,
                                                                 ORDER_STATUS_ENVIADO, CREATED_AT, UPDATED_AT);
        return orderResponseDTO;
    }

    @Test
    void orderCreate_shouldReturnCreated() throws Exception {
        OrderRequestDTO orderRequestDTO = buildOrderRequestDTO();
        OrderResponseDTO orderResponseDTO = buildOrderResponseDTO();

        when(orderService.createOrder(any(OrderRequestDTO.class))).thenReturn(orderResponseDTO);

        mockMvc.perform(post("/api/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(orderRequestDTO)))
               .andExpect(status().isCreated());
    }

    @Test
    void getById_shouldReturnOrder() throws Exception {
        OrderResponseDTO orderResponseDTO = buildOrderResponseDTO();

        when(orderService.getById(ORDER_ID)).thenReturn(orderResponseDTO);

        mockMvc.perform(get("/api/orders/{id}", ORDER_ID))
               .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ORDER_ID.toString()))
                .andExpect(jsonPath("$.status").value(ORDER_STATUS_PENDENTE.toString()));
    }

    @Test
    void getById_withInvalidId_shouldReturnException() throws Exception {
        UUID anotherId = UUID.randomUUID();
        when(orderService.getById(anotherId)).thenThrow(new OrderNotFoundException(ORDER_NOT_FOUND));

        mockMvc.perform(get("/api/orders/{id}", anotherId))
               .andExpect(status().isNotFound())
               .andExpect(jsonPath("$.status").value(404))
               .andExpect(jsonPath("$.message").value(ORDER_NOT_FOUND));
    }

    @Test
    void getByStatus_shouldReturnOrderList() throws Exception {
        OrderResponseDTO orderResponseDTO = buildOrderResponseDTO();

        when(orderService.getByStatus(ORDER_STATUS_PENDENTE)).thenReturn(List.of(orderResponseDTO));

        mockMvc.perform(get("/api/orders/status/{status}", ORDER_STATUS_PENDENTE))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(1)))
               .andExpect(jsonPath("$[0].status").value(ORDER_STATUS_PENDENTE.toString()));
    }

    @Test
    void getByStatus_withNoOrders_shouldReturnOrderEmptyList() throws Exception {
        when(orderService.getByStatus(ORDER_STATUS_ENVIADO)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/orders/status/{status}", ORDER_STATUS_ENVIADO))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getByPeriod_shouldReturnOrderList() throws Exception {
        OrderResponseDTO orderResponseDTO = buildOrderResponseDTO();

        when(orderService.getByPeriod(START_DATE, END_DATE)).thenReturn(List.of(orderResponseDTO));

        mockMvc.perform(get("/api/orders/period")
                                .param("start", START_DATE.toString())
                                .param("end", END_DATE.toString()))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getByPeriod_withNoOrders_shouldReturnEmptyOrderList() throws Exception {
        when(orderService.getByPeriod(START_DATE, END_DATE)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/orders/period")
                                .param("start", START_DATE.toString())
                                .param("end", END_DATE.toString()))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void orderStatusUpdate_shouldReturnOrderUpdated() throws Exception {
        OrderStatusUpdateRequestDTO orderStatusUpdateRequestDTO = new OrderStatusUpdateRequestDTO(ORDER_STATUS_ENVIADO);

        OrderResponseDTO orderResponseDTO = buildOrderEnviadoResponseDTO();

        when(orderService.updateStatus(ORDER_ID, orderStatusUpdateRequestDTO)).thenReturn(orderResponseDTO);

        mockMvc.perform(put("/api/orders/{id}/status", ORDER_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(orderStatusUpdateRequestDTO)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(ORDER_ID.toString()))
               .andExpect(jsonPath("$.status").value(ORDER_STATUS_ENVIADO.toString()));
    }

    @Test
    void orderStatusUpdate_withInvalidId_shouldReturnException() throws Exception {
        UUID anotherId = UUID.randomUUID();
        OrderStatusUpdateRequestDTO orderStatusUpdateRequestDTO = new OrderStatusUpdateRequestDTO(ORDER_STATUS_ENVIADO);

        when(orderService.updateStatus(anotherId, orderStatusUpdateRequestDTO)).thenThrow(new OrderNotFoundException(ORDER_NOT_FOUND));

        mockMvc.perform(put("/api/orders/{id}/status", anotherId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(orderStatusUpdateRequestDTO)))
               .andExpect(status().isNotFound())
               .andExpect(jsonPath("$.status").value(404))
               .andExpect(jsonPath("$.message").value(ORDER_NOT_FOUND));
    }

    @Test
    void cancelOrder_shouldReturnNoContent() throws Exception {
        doNothing().when(orderService).orderCancel(ORDER_ID);

        mockMvc.perform(put("/api/orders/{id}/cancel", ORDER_ID))
               .andExpect(status().isNoContent());
    }

    @Test
    void cancelOrder_withInvalidId_shouldReturnException() throws Exception {
        doThrow(new OrderNotFoundException(ORDER_NOT_FOUND)).when(orderService).orderCancel(ORDER_ID);

        mockMvc.perform(put("/api/orders/{id}/cancel", ORDER_ID))
               .andExpect(status().isNotFound())
               .andExpect(jsonPath("$.status").value(404))
               .andExpect(jsonPath("$.message").value(ORDER_NOT_FOUND));
    }

}