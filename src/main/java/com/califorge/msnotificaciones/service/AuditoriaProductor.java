package com.califorge.msnotificaciones.service;

import com.califorge.msnotificaciones.config.RabbitConfig;
import com.califorge.msnotificaciones.dto.AuditoriaMensaje;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Productor de la cola propia de auditoria.queue. Publica el resultado del
 * procesamiento de cada evento de usuario/orden/envio (incluido el resultado
 * del correo automatizado) sobre calisat.exchange. Best-effort: un fallo del
 * broker nunca interrumpe el envio del correo ni el listener que lo llama.
 */
@Component
public class AuditoriaProductor {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaProductor.class);

    private final RabbitTemplate rabbitTemplate;

    public AuditoriaProductor(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publicarUsuario(AuditoriaMensaje mensaje) {
        publicar(mensaje, RabbitConfig.ROUTING_KEY_AUDITORIA_USUARIO);
    }

    public void publicarOrden(AuditoriaMensaje mensaje) {
        publicar(mensaje, RabbitConfig.ROUTING_KEY_AUDITORIA_ORDEN);
    }

    private void publicar(AuditoriaMensaje mensaje, String routingKey) {
        try {
            rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, routingKey, mensaje);
            log.debug("Auditoria publicada: routingKey={} clave={}", routingKey, mensaje.clave());
        } catch (RuntimeException e) {
            log.warn("No se pudo publicar la auditoria (clave={}): {}", mensaje.clave(), e.getMessage());
        }
    }
}
