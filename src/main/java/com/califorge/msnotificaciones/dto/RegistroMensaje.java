package com.califorge.msnotificaciones.dto;

/**
 * Mensaje de registro recibido de ms-usuarios via RabbitMQ.
 * Debe coincidir con la estructura JSON publicada por el productor.
 */
public record RegistroMensaje(String identificador, String nombre, String email) {
}
