package com.califorge.msnotificaciones.dto;

/**
 * Mensaje de registro recibido de ms-usuarios via RabbitMQ.
 * Debe coincidir con la estructura JSON publicada por el productor.
 *
 * <p>Los mensajes antiguos solo traen los tres primeros campos: por eso
 * {@code nuevo} nulo se interpreta como alta nueva.</p>
 */
public record RegistroMensaje(String identificador, String nombre, String email,
                              String azureSub, String rol, Boolean nuevo) {

    public RegistroMensaje(String identificador, String nombre, String email) {
        this(identificador, nombre, email, null, null, null);
    }

    public boolean esAltaNueva() {
        return nuevo == null || nuevo;
    }
}
