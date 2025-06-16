package com.orderservice.event;

import com.orderservice.configuration.RabbitMQConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderStatusEventListener {

    @RabbitListener(queues = RabbitMQConfig.ORDER_STATUS_QUEUE)
    public void receiveEvent(OrderStatusEvent event) {
        log.info("Received order status event: {}", event);
        // Aqui pode-se processar, enviar notificações, etc.
    }

}
