package com.califorge.msnotificaciones.scheduler;

import com.califorge.msnotificaciones.client.CarritoClient;
import com.califorge.msnotificaciones.client.CarritoDto;
import com.califorge.msnotificaciones.dto.NotificacionEventoRequest;
import com.califorge.msnotificaciones.model.Canal;
import com.califorge.msnotificaciones.model.Destinatario;
import com.califorge.msnotificaciones.model.TipoNotificacion;
import com.califorge.msnotificaciones.service.DestinatarioService;
import com.califorge.msnotificaciones.service.NotificacionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Cron de carritos abandonados (fase B): cada dia a las 06:00 revisa los
 * carritos ABIERTO de los destinatarios activos del directorio y, si llevan
 * mas de {@value #DIAS_ABANDONO} dias sin actualizacion, ingesta una
 * notificacion TRANSACCIONAL (idempotente por carrito) que el poller de la
 * outbox despachara.
 *
 * <p>Lee el carrito via {@link CarritoClient} (calisat-ms-carrito). Todo
 * el proceso es best-effort: si el carrito esta caido, la pasada se omite
 * con un log y el MS de notificaciones sigue operando normalmente.</p>
 */
@Component
public class CarritoAbandonadoScheduler {

    static final int DIAS_ABANDONO = 30;

    private static final Logger log = LoggerFactory.getLogger(CarritoAbandonadoScheduler.class);

    private final CarritoClient carritoClient;
    private final DestinatarioService destinatarioService;
    private final NotificacionService notificacionService;

    public CarritoAbandonadoScheduler(CarritoClient carritoClient,
                                      DestinatarioService destinatarioService,
                                      NotificacionService notificacionService) {
        this.carritoClient = carritoClient;
        this.destinatarioService = destinatarioService;
        this.notificacionService = notificacionService;
    }

    @Scheduled(cron = "0 0 6 * * *")
    public void detectarCarritosAbandonados() {
        try {
            LocalDateTime limite = LocalDateTime.now().minusDays(DIAS_ABANDONO);
            int avisos = 0;
            for (Destinatario destinatario : destinatarioService.listar()) {
                if (destinatario.getActivo() != null && !destinatario.getActivo()) {
                    continue;
                }
                Optional<CarritoDto> posible = carritoClient.buscarPorUsuario(
                        destinatario.getAzureSub(), "ABIERTO");
                if (posible.isPresent() && esAbandonado(posible.get(), limite)) {
                    avisar(destinatario, posible.get());
                    avisos++;
                }
            }
            if (avisos > 0) {
                log.info("Carritos abandonados detectados: {}", avisos);
            }
        } catch (RuntimeException ex) {
            log.warn("Deteccion de carritos abandonados omitida (ms-carrito no disponible?): {}",
                    ex.getMessage());
        }
    }

    /** Carrito ABIERTO sin actualizacion desde hace al menos 30 dias. */
    private boolean esAbandonado(CarritoDto carrito, LocalDateTime limite) {
        if (carrito.fechaActualizacion() == null) {
            return false;
        }
        if (carrito.fechaActualizacion().isAfter(limite)) {
            return false;
        }
        return carrito.estado() == null || "ABIERTO".equals(carrito.estado());
    }

    /**
     * Ingesta idempotente del aviso: la clave 'carrito-abandonado-{id}'
     * evita re-avisar el mismo carrito en pasadas posteriores.
     */
    private void avisar(Destinatario destinatario, CarritoDto carrito) {
        String nombre = destinatario.getNombre() != null ? destinatario.getNombre() : "";
        NotificacionEventoRequest evento = new NotificacionEventoRequest(
                Canal.EMAIL,
                TipoNotificacion.TRANSACCIONAL,
                "Tu carrito te espera en Calisat",
                "Hola " + nombre + ", todavia tienes productos en tu carrito de compra. "
                        + "Completa tu compra cuando quieras.",
                null,
                destinatario.getAzureSub(),
                destinatario.getEmail(),
                destinatario.getNombre(),
                null,
                "{\"carritoId\":\"" + carrito.id() + "\",\"usuarioSub\":\"" + destinatario.getAzureSub() + "\"}",
                "calisat-ms-notificaciones",
                "carrito-abandonado");
        notificacionService.ingestar(evento, "carrito-abandonado-" + carrito.id());
    }
}
