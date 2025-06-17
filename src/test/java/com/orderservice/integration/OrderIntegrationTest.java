package com.orderservice.integration;

import com.orderservice.repository.OrderRepository;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
@RabbitListenerTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OrderIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

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

    @Test
    @Order(1)
    void saveOrderTest() {
        com.orderservice.entity.Order order = new com.orderservice.entity.Order();
        order.setId(UUID.randomUUID());
        order.setTotalAmount(new BigDecimal("100.00"));
        order.setStatus(com.orderservice.entity.Order.OrderStatus.PENDENTE);
        orderRepository.save(order);

        assertThat(orderRepository.findById(order.getId())).isPresent();
    }
}
