package com.califorge.msnotificaciones.exception;

public class NotificacionNoEncontradaException extends RuntimeException {

    public NotificacionNoEncontradaException(String id) {
        super("No existe ninguna notificacion con id '" + id + "'");
    }
}
