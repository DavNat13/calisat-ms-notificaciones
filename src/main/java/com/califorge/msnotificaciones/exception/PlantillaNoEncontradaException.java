package com.califorge.msnotificaciones.exception;

public class PlantillaNoEncontradaException extends RuntimeException {

    public PlantillaNoEncontradaException(String codigo) {
        super("No existe ninguna plantilla con el codigo '" + codigo + "'");
    }
}
