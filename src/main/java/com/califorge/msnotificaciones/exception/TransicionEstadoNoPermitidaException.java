package com.califorge.msnotificaciones.exception;

public class TransicionEstadoNoPermitidaException extends RuntimeException {

    public TransicionEstadoNoPermitidaException(String id, String desde, String hasta) {
        super("Transicion de estado no permitida para la notificacion '" + id
                + "': " + desde + " -> " + hasta);
    }
}
