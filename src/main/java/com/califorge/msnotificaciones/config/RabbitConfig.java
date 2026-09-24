package com.califorge.msnotificaciones.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuracion RabbitMQ del consumidor: cola de notificaciones enlazada
 * al exchange compartido calisat.exchange del productor (ms-usuarios).
 */
@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "calisat.exchange";
    public static final String QUEUE = "notificaciones.queue";
    public static final String ROUTING_KEY = "usuario.registrado";

    @Bean
    public DirectExchange calisatExchange() {
        return new DirectExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue notificacionesQueue() {
        return QueueBuilder.durable(QUEUE).build();
    }

    @Bean
    public Binding registroBinding() {
        return BindingBuilder.bind(notificacionesQueue()).to(calisatExchange()).with(ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
