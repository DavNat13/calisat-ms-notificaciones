package com.califorge.msnotificaciones.scheduler;

import com.califorge.msnotificaciones.service.NotificacionService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Poller de la tabla-outbox notificacion: cada 15s despacha las
 * notificaciones PENDIENTE/REINTENTO con proximo_intento_at vencido.
 * Sin Kafka/SQS en v1; la propia tabla es cola e historial.
 */
@Component
public class NotificacionPoller {

    private final NotificacionService notificacionService;

    public NotificacionPoller(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    @Scheduled(fixedDelay = 15000, initialDelay = 10000)
    public void despacharPendientes() {
        notificacionService.procesarPendientes();
    }
}
