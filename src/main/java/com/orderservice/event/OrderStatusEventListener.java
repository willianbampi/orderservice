package com.orderservice.event;

import com.orderservice.configuration.RabbitMQConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static com.orderservice.configuration.RabbitMQConfig.ORDER_STATUS_QUEUE;

@Slf4j
@Component
public class OrderStatusEventListener {

    private static final String RECEIVED_ORDER_STATUS_LOG_INFO = "Received order status event: {}";

    @RabbitListener(queues = ORDER_STATUS_QUEUE)
    public void receiveEvent(OrderStatusEvent event) {
        log.info(RECEIVED_ORDER_STATUS_LOG_INFO, event);
        //TODO improve the logic for consumer
    }

}