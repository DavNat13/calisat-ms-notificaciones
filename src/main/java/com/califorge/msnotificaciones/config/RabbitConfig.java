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
 * Configuracion RabbitMQ del consumidor: colas enlazadas al exchange
 * compartido calisat.exchange de los productores (ms-usuarios, ms-orden,
 * ms-envios), con conversor JSON.
 */
@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "calisat.exchange";

    public static final String QUEUE = "notificaciones.queue";
    public static final String ROUTING_KEY = "usuario.registrado";

    public static final String QUEUE_ORDENES = "ordenes.queue";
    public static final String ROUTING_KEY_ORDEN_CONFIRMADA = "orden.confirmada";
    public static final String ROUTING_KEY_ORDEN_CANCELADA = "orden.cancelada";

    public static final String QUEUE_ENVIOS = "envios.queue";
    public static final String ROUTING_KEY_ENVIO_DESPACHADO = "envio.despachado";
    public static final String ROUTING_KEY_ENVIO_ENTREGADO = "envio.entregado";

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
    public Queue ordenesQueue() {
        return QueueBuilder.durable(QUEUE_ORDENES).build();
    }

    @Bean
    public Binding ordenConfirmadaBinding() {
        return BindingBuilder.bind(ordenesQueue()).to(calisatExchange()).with(ROUTING_KEY_ORDEN_CONFIRMADA);
    }

    @Bean
    public Binding ordenCanceladaBinding() {
        return BindingBuilder.bind(ordenesQueue()).to(calisatExchange()).with(ROUTING_KEY_ORDEN_CANCELADA);
    }

    @Bean
    public Queue enviosQueue() {
        return QueueBuilder.durable(QUEUE_ENVIOS).build();
    }

    @Bean
    public Binding envioDespachadoBinding() {
        return BindingBuilder.bind(enviosQueue()).to(calisatExchange()).with(ROUTING_KEY_ENVIO_DESPACHADO);
    }

    @Bean
    public Binding envioEntregadoBinding() {
        return BindingBuilder.bind(enviosQueue()).to(calisatExchange()).with(ROUTING_KEY_ENVIO_ENTREGADO);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
