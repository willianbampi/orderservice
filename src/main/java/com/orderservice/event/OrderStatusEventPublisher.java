package com.orderservice.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import static com.orderservice.configuration.RabbitMQConfig.ORDER_STATUS_QUEUE;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderStatusEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishStatusChange(OrderStatusEvent event) {
        log.info("Sending order status event: {}", event);
        rabbitTemplate.convertAndSend(ORDER_STATUS_QUEUE, event);
    }

    public void publishStatusChange(com.orderservice.entity.Order order) {
        OrderStatusEvent event = new OrderStatusEvent(
                order.getId(),
                order.getPartnerId(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getUpdatedAt()
        );
        publishStatusChange(event);
    }

}
