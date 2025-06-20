package com.orderservice.service;

import com.orderservice.dto.OrderItemRequestDTO;
import com.orderservice.dto.OrderRequestDTO;
import com.orderservice.dto.OrderResponseDTO;
import com.orderservice.dto.OrderStatusUpdateRequestDTO;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class OrderServiceTest {

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

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private PartnerRepository partnerRepository;

    @Mock
    private OrderStatusEventPublisher eventPublisher;

    @InjectMocks
    private OrderService orderService;

    private OrderItem orderItem;
    private Order order;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        orderItem = new OrderItem();
        List<OrderItem> orderItemList = new ArrayList<OrderItem>();
        orderItemList.add(orderItem);

        order = new Order(ORDER_ID, PARTNER_ID, ORDER_TOTAL_AMOUNT, orderItemList, ORDER_STATUS_PENDENTE, CREATED_AT,
                          UPDATED_AT);

        orderItem.setId(ORDER_ITEM_ID);
        orderItem.setOrder(order);
        orderItem.setProductId(PRODUCT_ID);
        orderItem.setQuantity(1);
        orderItem.setUnitPrice(ORDER_ITEM_UNIT_PRICE);
        orderItem.setCreatedAt(CREATED_AT);
        orderItem.setUpdatedAt(UPDATED_AT);
        orderItemList.clear();
        orderItemList.add(orderItem);
        order.setItems(orderItemList);
    }

    private OrderRequestDTO buildOrderRequestDTO() {
        OrderItemRequestDTO item = new OrderItemRequestDTO(PRODUCT_ID, 1, ORDER_ITEM_UNIT_PRICE);
        List<OrderItemRequestDTO> orderItemRequestDTOList = new ArrayList<OrderItemRequestDTO>();
        orderItemRequestDTOList.add(item);
        OrderRequestDTO orderRequestDTO = new OrderRequestDTO(PARTNER_ID, orderItemRequestDTOList);
        return orderRequestDTO;
    }

    @Test
    void createOrder_withSuficientCredit_shouldCreateOrder() {
        OrderRequestDTO orderRequestDTO = buildOrderRequestDTO();

        when(partnerRepository.findById(PARTNER_ID)).thenReturn(Optional.of(PARTNER));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderResponseDTO orderResponseDTO = orderService.createOrder(orderRequestDTO);

        assertNotNull(orderResponseDTO);
        assertEquals(ORDER_ID, orderResponseDTO.id());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void createOrder_withInsuficientCredit_shouldThrowsException() {
        OrderItemRequestDTO item = new OrderItemRequestDTO(PRODUCT_ID, 1, ORDER_ITEM_UNIT_PRICE_NEW);
        List<OrderItemRequestDTO> orderItemList = new ArrayList<OrderItemRequestDTO>();
        orderItemList.add(item);
        OrderRequestDTO orderRequestDTO = new OrderRequestDTO(PARTNER_ID, orderItemList);

        when(partnerRepository.findById(PARTNER.getId())).thenReturn(Optional.of(PARTNER));

        assertThrows(InsufficientCreditException.class, () -> orderService.createOrder(orderRequestDTO));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_withInvalidParter_shouldThrowsException() {
        OrderRequestDTO orderRequestDTO = buildOrderRequestDTO();

        when(partnerRepository.findById(UUID.randomUUID())).thenReturn(Optional.empty());

        assertThrows(PartnerNotFoundException.class, () -> orderService.createOrder(orderRequestDTO));

        verify(orderRepository, never()).save(any());
    }

    @Test
    void getById_shouldReturnOrder() {
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        OrderResponseDTO orderResponseDTO = orderService.getById(ORDER_ID);

        assertNotNull(orderResponseDTO);
        assertEquals(ORDER_ID, orderResponseDTO.id());
    }

    @Test
    void getById_withInvalidId_shouldReturnException() {
        UUID anotherId = UUID.randomUUID();
        when(orderRepository.findById(anotherId)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderService.getById(anotherId));
    }

    @Test
    void getByStatus_shouldReturnOrderList() {
        List<Order> orderList = List.of(order);

        when(orderRepository.findByStatus(ORDER_STATUS_PENDENTE)).thenReturn(orderList);

        List<OrderResponseDTO> orderResponseDTOList = orderService.getByStatus(ORDER_STATUS_PENDENTE);

        assertFalse(orderResponseDTOList.isEmpty());
        assertEquals(1, orderResponseDTOList.size());
    }

    @Test
    void getByStatus_shouldReturnEmptyOrderList() {
        when(orderRepository.findByStatus(ORDER_STATUS_ENVIADO)).thenReturn(Collections.emptyList());

        List<OrderResponseDTO> orderResponseDTOList = orderService.getByStatus(ORDER_STATUS_ENVIADO);

        assertTrue(orderResponseDTOList.isEmpty());
    }

    @Test
    void getByPeriod_shouldReturnOrderList() {
        List<Order> orderList = List.of(order);

        when(orderRepository.findByCreatedAtBetween(START_DATE, END_DATE)).thenReturn(orderList);

        List<OrderResponseDTO> orderResponseDTOList = orderService.getByPeriod(START_DATE, END_DATE);

        assertFalse(orderResponseDTOList.isEmpty());
    }

    @Test
    void getByPeriod_shouldReturnEmptyOrderList() {
        when(orderRepository.findByCreatedAtBetween(START_DATE, END_DATE)).thenReturn(Collections.emptyList());

        List<OrderResponseDTO> orderResponseDTOList = orderService.getByPeriod(START_DATE, END_DATE);

        assertTrue(orderResponseDTOList.isEmpty());
    }

    @Test
    void updateStatus_shouldUpdateStatusOrder() {
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        OrderStatusUpdateRequestDTO orderStatusUpdateRequestDTO = new OrderStatusUpdateRequestDTO(ORDER_STATUS_ENVIADO);

        OrderResponseDTO orderResponseDTO = orderService.updateStatus(ORDER_ID, orderStatusUpdateRequestDTO);

        assertNotNull(orderResponseDTO);
        assertEquals(ORDER_STATUS_ENVIADO, orderResponseDTO.status());
    }

    @Test
    void updateStatus_withInvalidId_shouldReturnException() {
        UUID anotherId = UUID.randomUUID();
        when(orderRepository.findById(anotherId)).thenReturn(Optional.empty());

        OrderStatusUpdateRequestDTO orderStatusUpdateRequestDTO = new OrderStatusUpdateRequestDTO(ORDER_STATUS_ENVIADO);

        assertThrows(OrderNotFoundException.class, () -> orderService.updateStatus(anotherId, orderStatusUpdateRequestDTO));
    }

    @Test
    void cancelOrder_shouldCancelOrder() {
        when(partnerRepository.findById(PARTNER_ID)).thenReturn(Optional.of(PARTNER));
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        orderService.orderCancel(ORDER_ID);
    }

    @Test
    void cancelOrder_withInvalidId_shouldReturnException() {
        UUID anotherId = UUID.randomUUID();
        when(orderRepository.findById(anotherId)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderService.orderCancel(anotherId));
    }

}