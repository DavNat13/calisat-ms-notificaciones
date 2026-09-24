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

    /**
     * Aviso de orden confirmada (pago aceptado) hacia el cliente.
     */
    public void enviarOrdenConfirmada(String email, String ordenId, String total) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(email);
        mensaje.setSubject("Tu orden ha sido confirmada");
        String detalleTotal = (total != null && !total.isBlank()) ? " con total " + total : "";
        mensaje.setText(
                "Hola:\n\n"
                        + "Tu orden " + ordenId + " fue confirmada" + detalleTotal + ".\n\n"
                        + "Prepararemos tu pedido y podrás seguir su estado desde tu cuenta.\n\n"
                        + "Saludos equipo Calisat");
        mailSender.send(mensaje);
        log.info("Correo de orden confirmada enviado a {}", email);
    }

    /**
     * Aviso de orden cancelada hacia el cliente.
     */
    public void enviarOrdenCancelada(String email, String ordenId) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(email);
        mensaje.setSubject("Tu orden fue cancelada");
        mensaje.setText(
                "Hola:\n\n"
                        + "Tu orden " + ordenId + " fue cancelada.\n\n"
                        + "Si no fuiste tú quien la canceló, contáctanos para revisarlo.\n\n"
                        + "Saludos equipo Calisat");
        mailSender.send(mensaje);
        log.info("Correo de orden cancelada enviado a {}", email);
    }

    /**
     * Aviso de envio despachado (va en camino) hacia el cliente.
     */
    public void enviarEnvioDespachado(String email, String numeroGuia, String transportista) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(email);
        mensaje.setSubject("Tu envío va en camino");
        String detalleTransportista = (transportista != null && !transportista.isBlank())
                ? " con " + transportista : "";
        mensaje.setText(
                "Hola:\n\n"
                        + "Tu envío con guía " + numeroGuia + " fue despachado"
                        + detalleTransportista + ".\n\n"
                        + "Puedes seguirlo desde el seguimiento de tu pedido.\n\n"
                        + "Saludos equipo Calisat");
        mailSender.send(mensaje);
        log.info("Correo de envio despachado enviado a {}", email);
    }

    /**
     * Aviso de envio entregado hacia el cliente.
     */
    public void enviarEnvioEntregado(String email, String numeroGuia) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(email);
        mensaje.setSubject("Tu envío ha sido entregado");
        mensaje.setText(
                "Hola:\n\n"
                        + "Tu envío con guía " + numeroGuia + " ha sido entregado.\n\n"
                        + "Gracias por comprar en Calisat.\n\n"
                        + "Saludos equipo Calisat");
        mailSender.send(mensaje);
        log.info("Correo de envio entregado enviado a {}", email);
    }
}
