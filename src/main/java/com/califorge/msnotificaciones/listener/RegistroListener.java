package com.califorge.msnotificaciones.listener;

import com.califorge.msnotificaciones.dto.AuditoriaMensaje;
import com.califorge.msnotificaciones.dto.RegistroMensaje;
import com.califorge.msnotificaciones.model.ResultadoAuditoria;
import com.califorge.msnotificaciones.model.TipoAuditoria;
import com.califorge.msnotificaciones.service.AuditoriaProductor;
import com.califorge.msnotificaciones.service.DestinatarioService;
import com.califorge.msnotificaciones.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumidor del evento de registro publicado por ms-usuarios
 * en la cola notificaciones.queue. Registra al usuario como destinatario y,
 * si es un alta nueva, envia el correo de bienvenida y publica el resultado en
 * la cola propia de auditoria (auditoria.queue).
 */
@Component
public class RegistroListener {

    private static final Logger log = LoggerFactory.getLogger(RegistroListener.class);

    private final EmailService emailService;
    private final AuditoriaProductor auditoriaProductor;

    private final DestinatarioService destinatarioService;

    public RegistroListener(EmailService emailService, AuditoriaProductor auditoriaProductor,
                            DestinatarioService destinatarioService) {
        this.destinatarioService = destinatarioService;
        this.emailService = emailService;
        this.auditoriaProductor = auditoriaProductor;
    }

    @RabbitListener(queues = "notificaciones.queue")
    public void onUsuarioRegistrado(RegistroMensaje mensaje) {
        log.info("Evento de registro recibido: id={} email={}", mensaje.identificador(), mensaje.email());

        destinatarioService.sincronizar(
                mensaje.azureSub(), mensaje.email(), mensaje.nombre(), mensaje.rol());
        if (!mensaje.esAltaNueva()) {
            return;
        }

        ResultadoAuditoria resultado;
        String detalle = null;
        try {
            emailService.enviarBienvenida(mensaje.email(), mensaje.nombre());
            resultado = ResultadoAuditoria.ENVIADO;
        } catch (RuntimeException e) {
            resultado = ResultadoAuditoria.FALLIDO;
            detalle = e.getMessage();
            log.warn("Correo de bienvenida fallido para {}: {}", mensaje.email(), e.getMessage());
        }

        auditoriaProductor.publicarUsuario(AuditoriaMensaje.de(
                TipoAuditoria.USUARIO,
                "usuario.registrado",
                mensaje.identificador(),
                null,
                mensaje.email(),
                resultado,
                detalle));
    }
}
