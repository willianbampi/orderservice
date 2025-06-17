package com.orderservice.event;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class EventServiceTest {

    @Test
    void sendNotification_shouldSendMessage() {
        RabbitTemplate rabbitTemplate = Mockito.mock(RabbitTemplate.class);
        OrderStatusEventPublisher orderStatusEventPublisher = new OrderStatusEventPublisher(rabbitTemplate);
        OrderStatusEvent orderStatusEvent = new OrderStatusEvent(UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("1000.00"), com.orderservice.entity.Order.OrderStatus.PENDENTE, new LocalDate.of(2025, 10, 10));

        orderStatusEventPublisher.publishStatusChange(orderStatusEvent);

        Mockito.verify(rabbitTemplate, Mockito.times(1))
                .convertAndSend(Mockito.eq("order.status.queue"), Mockito.eq("order.status.update"), Mockito.anyString());
    }

}
