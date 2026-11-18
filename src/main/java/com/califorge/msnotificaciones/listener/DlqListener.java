package com.califorge.msnotificaciones.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Consumidor de la DLQ general (dlq.general.queue). Los mensajes que agotan
 * reintentos o son rechazados llegan aqui via calisat.dlx; se registran en logs
 * con su causa (cabecera x-death) para trazabilidad. No se reencolan.
 */
@Component
public class DlqListener {

    private static final Logger log = LoggerFactory.getLogger(DlqListener.class);

    @RabbitListener(queues = "dlq.general.queue")
    public void onMensajeMuerto(Message mensaje) {
        Map<String, Object> headers = mensaje.getMessageProperties().getHeaders();
        String cuerpo = new String(mensaje.getBody(), StandardCharsets.UTF_8);
        log.error("[DLQ] Mensaje no entregado. colaOrigen={} routingKey={} x-death={} cuerpo={}",
                mensaje.getMessageProperties().getConsumerQueue(),
                mensaje.getMessageProperties().getReceivedRoutingKey(),
                headers.get("x-death"), cuerpo);
    }
}
