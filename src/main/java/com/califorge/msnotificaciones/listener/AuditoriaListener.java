package com.califorge.msnotificaciones.listener;

import com.califorge.msnotificaciones.config.RabbitConfig;
import com.califorge.msnotificaciones.dto.AuditoriaMensaje;
import com.califorge.msnotificaciones.service.AuditoriaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumidor de la cola propia auditoria.queue: persiste cada evento de
 * auditoria (usuario/orden/envio + resultado del correo) en la tabla
 * auditoria_evento. Productor y consumidor viven en este mismo microservicio.
 */
@Component
public class AuditoriaListener {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaListener.class);

    private final AuditoriaService auditoriaService;

    public AuditoriaListener(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE_AUDITORIA)
    public void onAuditoria(AuditoriaMensaje mensaje) {
        if (mensaje == null || mensaje.clave() == null || mensaje.clave().isBlank()) {
            log.warn("Mensaje de auditoria sin clave, se omite");
            return;
        }
        auditoriaService.registrar(mensaje);
    }
}
