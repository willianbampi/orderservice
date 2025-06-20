package com.orderservice.configuration;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

@Configuration
public class RabbitMQConfig {

    // 📦 Exchanges
    public static final String ORDER_EVENT_EXCHANGE = "order.event.exchange";

    // 🎯 QUEUES
    public static final String ORDER_STATUS_QUEUE = "order.status.event";
    public static final String ORDER_STATUS_DLQ = "order.status.event.dlq";

    // 🔗 Routing Key
    public static final String ORDER_STATUS_ROUTING_KEY = "order.status.event";

    // Exchange
    @Bean
    public DirectExchange orderEventExchange() {
        return new DirectExchange(ORDER_EVENT_EXCHANGE);
    }

    // Main Queue with DLQ
    @Bean
    public Queue orderStatusQueue() {
        return QueueBuilder.durable(ORDER_STATUS_QUEUE)
                           .withArgument("x-dead-letter-exchange", ORDER_EVENT_EXCHANGE)
                           .withArgument("x-dead-letter-routing-key", ORDER_STATUS_DLQ)
                           .build();
    }

    // DLQ
    @Bean
    public Queue orderStatusDlq() {
        return QueueBuilder.durable(ORDER_STATUS_DLQ).build();
    }

    // Bindings
    @Bean
    public Binding orderStatusBinding() {
        return BindingBuilder
                .bind(orderStatusQueue())
                .to(orderEventExchange())
                .with(ORDER_STATUS_ROUTING_KEY);
    }

    @Bean
    public Binding orderStatusDlqBinding() {
        return BindingBuilder
                .bind(orderStatusDlq())
                .to(orderEventExchange())
                .with(ORDER_STATUS_DLQ);
    }

    // Automatic retry with backoff
    @Bean
    public RetryOperationsInterceptor retryInterceptor() {
        return RetryInterceptorBuilder.stateless()
                                      .maxAttempts(3)
                                      .backOffOptions(1000, 2.0, 10000)
                                      .recoverer(new RejectAndDontRequeueRecoverer())
                                      .build();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            RetryOperationsInterceptor retryInterceptor) {

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setAdviceChain(retryInterceptor);
        return factory;
    }

    @Bean
    public MessageConverter jacksonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        converter.setClassMapper(classMapper());
        return converter;
    }

    @Bean
    public DefaultClassMapper classMapper() {
        DefaultClassMapper classMapper = new DefaultClassMapper();
        classMapper.setTrustedPackages(
                "com.orderservice.event",
                "java.util",
                "java.lang"
        );
        return classMapper;
    }

}