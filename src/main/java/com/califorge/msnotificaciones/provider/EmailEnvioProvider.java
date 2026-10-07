package com.califorge.msnotificaciones.provider;

import com.califorge.msnotificaciones.model.Notificacion;
import com.califorge.msnotificaciones.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Despacho real por SMTP (JavaMailSender) de las notificaciones ingeridas
 * via /eventos y /enviar. Sustituye al no-op LogEnvioProvider.
 *
 * <p>Seleccion con {@code notificaciones.envio.proveedor} =
 * {@code email} (defecto) o {@code log} (pruebas/CI).</p>
 */
@Component
@ConditionalOnProperty(name = "notificaciones.envio.proveedor",
        havingValue = "email", matchIfMissing = true)
public class EmailEnvioProvider implements EnvioProvider {

    private static final Logger log = LoggerFactory.getLogger(EmailEnvioProvider.class);

    private final EmailService emailService;

    public EmailEnvioProvider(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public ResultadoEnvio enviar(Notificacion notificacion) {
        String destino = notificacion.getDestinatarioEmail();
        if (destino == null || destino.isBlank()) {
            return ResultadoEnvio.fallo("SMTP", null,
                    "Sin destinatarioEmail resuelto para sub="
                            + notificacion.getDestinatarioSub(), false);
        }
        if (notificacion.getAsunto() == null || notificacion.getAsunto().isBlank()) {
            return ResultadoEnvio.fallo("SMTP", null, "La notificacion no tiene asunto", false);
        }
        try {
            emailService.enviar(destino, notificacion.getAsunto(), notificacion.getCuerpoTexto());
            return ResultadoEnvio.exito("SMTP", "SMTP-" + UUID.randomUUID());
        } catch (MailException ex) {
            log.warn("Fallo SMTP al enviar a {}: {}", destino, ex.getMessage());
            return ResultadoEnvio.fallo("SMTP", null, ex.getMessage(), true);
        } catch (RuntimeException ex) {
            log.error("Error inesperado despachando a {}: {}", destino, ex.getMessage());
            return ResultadoEnvio.fallo("SMTP", null, ex.getMessage(), false);
        }
    }
}
