package com.orderservice.event;

import com.orderservice.entity.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
public class EventServiceTest {

    private static final UUID ORDER_ID = UUID.randomUUID();
    private static final UUID PARTNER_ID = UUID.randomUUID();
    private static final BigDecimal TOTAL_AMOUNT = new BigDecimal("500.00");
    private static final com.orderservice.entity.Order.OrderStatus STATUS_PENDENTE = Order.OrderStatus.PENDENTE;
    private static final LocalDateTime CREATED_AT = LocalDate.of(2020, Month.JANUARY, 18).atStartOfDay();

    @InjectMocks
    private OrderStatusEventListener orderStatusEventListener;

    @Test
    void shouldLogEvent() {
        OrderStatusEvent orderStatusEvent = new OrderStatusEvent(ORDER_ID, PARTNER_ID, TOTAL_AMOUNT, STATUS_PENDENTE, CREATED_AT);

        assertDoesNotThrow(() -> orderStatusEventListener.receiveEvent(orderStatusEvent));
    }

}