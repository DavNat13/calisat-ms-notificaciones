package com.califorge.msnotificaciones.listener;

import com.califorge.msnotificaciones.dto.AuditoriaMensaje;
import com.califorge.msnotificaciones.dto.OrdenMensaje;
import com.califorge.msnotificaciones.model.Destinatario;
import com.califorge.msnotificaciones.model.ResultadoAuditoria;
import com.califorge.msnotificaciones.model.TipoAuditoria;
import com.califorge.msnotificaciones.repository.DestinatarioRepository;
import com.califorge.msnotificaciones.service.AuditoriaProductor;
import com.califorge.msnotificaciones.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Consumidor de los eventos de orden publicados por ms-orden en la cola
 * ordenes.queue (orden.confirmada / orden.cancelada). Resuelve el email del
 * destinatario en el directorio, envia el correo contextual correspondiente
 * y publica el resultado en la cola propia de auditoria (auditoria.queue).
 */
@Component
public class OrdenListener {

    private static final Logger log = LoggerFactory.getLogger(OrdenListener.class);

    private final EmailService emailService;
    private final DestinatarioRepository destinatarioRepository;
    private final AuditoriaProductor auditoriaProductor;

    public OrdenListener(EmailService emailService, DestinatarioRepository destinatarioRepository,
                         AuditoriaProductor auditoriaProductor) {
        this.emailService = emailService;
        this.destinatarioRepository = destinatarioRepository;
        this.auditoriaProductor = auditoriaProductor;
    }

    @RabbitListener(queues = "ordenes.queue")
    public void onOrdenMensaje(OrdenMensaje mensaje) {
        if (mensaje == null || mensaje.evento() == null || mensaje.evento().isBlank()) {
            log.warn("Mensaje de orden sin evento, se omite");
            return;
        }
        log.info("Evento de orden recibido: evento={} ordenId={}", mensaje.evento(), mensaje.ordenId());

        Optional<String> email = emailDe(mensaje.usuarioSub());

        ResultadoAuditoria resultado;
        String detalle = null;
        String emailDestino = email.orElse(null);

        if (email.isEmpty()) {
            resultado = ResultadoAuditoria.SIN_DESTINATARIO;
            detalle = "Sin destinatario activo con email para el sub " + mensaje.usuarioSub();
            log.warn("Sin email resoluble para el sub {} (evento {}), correo omitido",
                    mensaje.usuarioSub(), mensaje.evento());
        } else {
            try {
                switch (mensaje.evento()) {
                    case "ORDEN_CONFIRMADA" -> {
                        emailService.enviarOrdenConfirmada(email.get(), mensaje.ordenId(), mensaje.total());
                        resultado = ResultadoAuditoria.ENVIADO;
                    }
                    case "ORDEN_CANCELADA" -> {
                        emailService.enviarOrdenCancelada(email.get(), mensaje.ordenId());
                        resultado = ResultadoAuditoria.ENVIADO;
                    }
                    default -> {
                        resultado = ResultadoAuditoria.EVENTO_DESCONOCIDO;
                        detalle = "Evento de orden desconocido: " + mensaje.evento();
                        log.warn("Evento de orden desconocido: {}", mensaje.evento());
                    }
                }
            } catch (RuntimeException e) {
                resultado = ResultadoAuditoria.FALLIDO;
                detalle = e.getMessage();
                log.warn("Correo de orden fallido para {}: {}", email.get(), e.getMessage());
            }
        }

        auditoriaProductor.publicarOrden(AuditoriaMensaje.de(
                TipoAuditoria.ORDEN,
                mensaje.evento(),
                mensaje.ordenId(),
                mensaje.usuarioSub(),
                emailDestino,
                resultado,
                detalle));
    }

    /** Resuelve el email del directorio solo si el destinatario esta activo. */
    private Optional<String> emailDe(String azureSub) {
        if (azureSub == null || azureSub.isBlank()) {
            return Optional.empty();
        }
        return destinatarioRepository.findByAzureSub(azureSub)
                .filter(destinatario -> Boolean.TRUE.equals(destinatario.getActivo()))
                .map(Destinatario::getEmail)
                .filter(email -> email != null && !email.isBlank());
    }
}
