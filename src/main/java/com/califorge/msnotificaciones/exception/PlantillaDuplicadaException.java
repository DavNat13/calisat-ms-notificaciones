package com.califorge.msnotificaciones.exception;

public class PlantillaDuplicadaException extends RuntimeException {

    public PlantillaDuplicadaException(String codigo) {
        super("Ya existe una plantilla con el codigo '" + codigo + "'");
    }
}
