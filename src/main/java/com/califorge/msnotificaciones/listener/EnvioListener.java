package com.califorge.msnotificaciones.listener;

import com.califorge.msnotificaciones.dto.EnvioMensaje;
import com.califorge.msnotificaciones.model.Destinatario;
import com.califorge.msnotificaciones.repository.DestinatarioRepository;
import com.califorge.msnotificaciones.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Consumidor de los eventos de envio publicados por ms-envios en la cola
 * envios.queue (envio.despachado / envio.entregado). Resuelve el email del
 * destinatario en el directorio y envia el correo contextual correspondiente.
 */
@Component
public class EnvioListener {

    private static final Logger log = LoggerFactory.getLogger(EnvioListener.class);

    private final EmailService emailService;
    private final DestinatarioRepository destinatarioRepository;

    public EnvioListener(EmailService emailService, DestinatarioRepository destinatarioRepository) {
        this.emailService = emailService;
        this.destinatarioRepository = destinatarioRepository;
    }

    @RabbitListener(queues = "envios.queue")
    public void onEnvioMensaje(EnvioMensaje mensaje) {
        if (mensaje == null || mensaje.evento() == null || mensaje.evento().isBlank()) {
            log.warn("Mensaje de envio sin evento, se omite");
            return;
        }
        log.info("Evento de envio recibido: evento={} guia={}", mensaje.evento(), mensaje.numeroGuia());

        Optional<String> email = emailDe(mensaje.usuarioSub());
        if (email.isEmpty()) {
            log.warn("Sin email resoluble para el sub {} (evento {}), correo omitido",
                    mensaje.usuarioSub(), mensaje.evento());
            return;
        }

        switch (mensaje.evento()) {
            case "ENVIO_DESPACHADO" ->
                    emailService.enviarEnvioDespachado(email.get(), mensaje.numeroGuia(), mensaje.transportista());
            case "ENVIO_ENTREGADO" ->
                    emailService.enviarEnvioEntregado(email.get(), mensaje.numeroGuia());
            default -> log.warn("Evento de envio desconocido: {}", mensaje.evento());
        }
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
