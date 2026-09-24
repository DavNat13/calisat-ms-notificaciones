package com.califorge.msnotificaciones.listener;

import com.califorge.msnotificaciones.dto.RegistroMensaje;
import com.califorge.msnotificaciones.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumidor del evento de registro publicado por ms-usuarios
 * en la cola notificaciones.queue.
 */
@Component
public class RegistroListener {

    private static final Logger log = LoggerFactory.getLogger(RegistroListener.class);

    private final EmailService emailService;

    public RegistroListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @RabbitListener(queues = "notificaciones.queue")
    public void onUsuarioRegistrado(RegistroMensaje mensaje) {
        log.info("Evento de registro recibido: id={} email={}", mensaje.identificador(), mensaje.email());
        emailService.enviarBienvenida(mensaje.email(), mensaje.nombre());
    }
}
