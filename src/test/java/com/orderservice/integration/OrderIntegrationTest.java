package com.orderservice.integration;

import com.orderservice.entity.Order;
import com.orderservice.entity.OrderItem;
import com.orderservice.entity.Partner;
import com.orderservice.repository.OrderRepository;
import com.orderservice.repository.PartnerRepository;
import org.junit.jupiter.api.*;
import org.springframework.amqp.rabbit.test.RabbitListenerTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
@RabbitListenerTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OrderIntegrationTest {

    private static final UUID PARTNER_ID = UUID.randomUUID();
    private static final UUID PRODUCT_ID = UUID.randomUUID();
    private static final UUID ORDER_ITEM_ID = UUID.randomUUID();
    private static final UUID ORDER_ID = UUID.randomUUID();
    private static final String PARTNER_A_NAME = "Partner A";
    private static final String PARTNER_UPDATE_NAME = "Partner Update";
    private static final BigDecimal ORDER_TOTAL_AMOUNT = new BigDecimal("500.00");
    private static final BigDecimal ORDER_ITEM_UNIT_PRICE = new BigDecimal("500.00");
    private static final BigDecimal ORDER_ITEM_UNIT_PRICE_NEW = new BigDecimal("1500.00");
    private static final BigDecimal CREDIT_LIMIT_INITIAL = new BigDecimal("1000.00");
    private static final BigDecimal CREDIT_LIMIT_UPDATED = new BigDecimal("2000.00");
    private static final com.orderservice.entity.Order.OrderStatus ORDER_STATUS_PENDENTE = Order.OrderStatus.PENDENTE;
    private static final com.orderservice.entity.Order.OrderStatus ORDER_STATUS_ENVIADO = Order.OrderStatus.ENVIADO;
    private static final com.orderservice.entity.Order.OrderStatus ORDER_STATUS_ENTREGUE = Order.OrderStatus.ENTREGUE;
    private static final com.orderservice.entity.Order.OrderStatus ORDER_STATUS_CANCELADO = Order.OrderStatus.CANCELADO;
    private static final LocalDateTime CREATED_AT = LocalDate.of(2020, Month.JANUARY, 18).atStartOfDay();
    private static final LocalDateTime UPDATED_AT = LocalDate.of(2020, Month.JANUARY, 18).atStartOfDay();
    private static final LocalDateTime START_DATE = LocalDate.of(2020, Month.JANUARY, 17).atStartOfDay();
    private static final LocalDateTime END_DATE = LocalDate.of(2020, Month.JANUARY, 19).atStartOfDay();
    private static final String PARTNER_NOT_FOUND = "Partner not found!";

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PartnerRepository partnerRepository;

    private OrderItem orderItem;
    private com.orderservice.entity.Order order;
    private Partner partner;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.3")
            .withDatabaseName("orderdb")
            .withUsername("postgres")
            .withPassword("postgres");

    @Container
    static RabbitMQContainer rabbitMQ = new RabbitMQContainer("rabbitmq:3.11-management")
            .withExposedPorts(5672, 15672);

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.rabbitmq.host", rabbitMQ::getHost);
        registry.add("spring.rabbitmq.port", rabbitMQ::getAmqpPort);
        registry.add("spring.rabbitmq.username", () -> "guest");
        registry.add("spring.rabbitmq.password", () -> "guest");
    }

    @BeforeEach
    void setup() {
        partner = new Partner(PARTNER_ID, PARTNER_A_NAME, CREDIT_LIMIT_INITIAL, CREATED_AT, UPDATED_AT);

        orderItem = new OrderItem();
        List<OrderItem> orderItemList = new ArrayList<OrderItem>();
        orderItemList.add(orderItem);

        order = new com.orderservice.entity.Order(ORDER_ID, PARTNER_ID, ORDER_TOTAL_AMOUNT, orderItemList, ORDER_STATUS_PENDENTE, CREATED_AT,
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

        order = orderRepository.save(order);
    }

    @AfterEach
    void tearDown() {
        orderRepository.deleteAll();
        partnerRepository.deleteAll();
    }

    @Test
    void shouldCreateOrder() {
        OrderItem newOrderItem = new OrderItem();
        List<OrderItem> newOrderItemList = new ArrayList<OrderItem>();
        newOrderItemList.add(newOrderItem);

        UUID newOrderId = UUID.randomUUID();
        com.orderservice.entity.Order newOrder = new com.orderservice.entity.Order(newOrderId, PARTNER_ID, ORDER_TOTAL_AMOUNT,
                                                           newOrderItemList, ORDER_STATUS_PENDENTE, CREATED_AT, UPDATED_AT);

        newOrderItem.setId(ORDER_ITEM_ID);
        newOrderItem.setOrder(order);
        newOrderItem.setProductId(PRODUCT_ID);
        newOrderItem.setQuantity(1);
        newOrderItem.setUnitPrice(ORDER_ITEM_UNIT_PRICE);
        newOrderItem.setCreatedAt(CREATED_AT);
        newOrderItem.setUpdatedAt(UPDATED_AT);
        newOrderItemList.clear();
        newOrderItemList.add(newOrderItem);
        newOrder.setItems(newOrderItemList);


        com.orderservice.entity.Order newOrderSaved = orderRepository.save(newOrder);

        assertNotNull(newOrderSaved.getId());
        assertEquals(ORDER_STATUS_PENDENTE, newOrderSaved.getStatus());
        assertEquals(1, newOrderSaved.getItems().size());
    }

    @Test
    void shouldGetOrderById() {
        Optional<Order> orderSearched = orderRepository.findById(order.getId());

        assertTrue(orderSearched.isPresent());
        assertEquals(order.getId(), orderSearched.get().getId());
        assertEquals(ORDER_STATUS_PENDENTE, orderSearched.get().getStatus());
    }

    @Test
    void shouldReturnEmptyWhenOrderIdInvalid() {
        Optional<Order> orderSearched = orderRepository.findById(UUID.randomUUID());

        assertTrue(orderSearched.isEmpty());
    }

    @Test
    void shouldGetOrderByStatus() {
        List<Order> orderList = orderRepository.findByStatus(ORDER_STATUS_PENDENTE);

        assertFalse(orderList.isEmpty());
        assertEquals(ORDER_STATUS_PENDENTE, orderList.get(0).getStatus());
    }

    @Test
    void shouldReturnEmptyWhenOrderStatusInvalid() {
        List<Order> orderList = orderRepository.findByStatus(ORDER_STATUS_ENVIADO);

        assertTrue(orderList.isEmpty());
    }

    @Test
    void shouldGetOrderByPeriod() {
        List<Order> orderList = orderRepository.findByCreatedAtBetween(START_DATE, END_DATE);

        assertFalse(orderList.isEmpty());
        assertTrue(orderList.stream().allMatch(o -> !o.getCreatedAt().isBefore(START_DATE) && !o.getCreatedAt().isAfter(END_DATE)));
    }

    @Test
    void shouldReturnEmptyWhenOrderPeriodInvalid() {
        List<Order> orderList = orderRepository.findByCreatedAtBetween(START_DATE.plusDays(10), END_DATE.plusDays(10));

        assertTrue(orderList.isEmpty());
    }

    @Test
    void shouldUpdateOrderStatus() {
        order.setStatus(ORDER_STATUS_ENTREGUE);
        Order orderUpdated = orderRepository.save(order);

        Optional<Order> orderSearched = orderRepository.findById(orderUpdated.getId());

        assertTrue(orderSearched.isPresent());
        assertEquals(ORDER_STATUS_ENTREGUE, orderSearched.get().getStatus());
    }

    @Test
    void shouldCancelOrder() {
        order.setStatus(ORDER_STATUS_CANCELADO);
        Order orderCanceled = orderRepository.save(order);

        Optional<Order> orderSearched = orderRepository.findById(orderCanceled.getId());

        assertTrue(orderSearched.isPresent());
        assertEquals(ORDER_STATUS_CANCELADO, orderSearched.get().getStatus());
    }

}