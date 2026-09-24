package com.califorge.msnotificaciones.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Envio de correos electronicos via SMTP (JavaMailSender).
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Envia el correo de bienvenida al usuario recien registrado.
     */
    public void enviarBienvenida(String email, String nombre) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(email);
        mensaje.setSubject("Bienvenido a Calisat");
        String saludo = (nombre != null && !nombre.isBlank()) ? nombre : "usuario";
        mensaje.setText(
                "Hola " + saludo + ":\n\n"
                        + "¡Gracias por registrarte en Calisat!\n\n"
                        + "Tu cuenta ya está lista. Inicia sesión para explorar "
                        + "nuestro catálogo, gestionar tu carrito y realizar tus compras.\n\n"
                        + "Saludos equipo Calisat");
        mailSender.send(mensaje);
        log.info("Correo de bienvenida enviado a {}", email);
    }
}
