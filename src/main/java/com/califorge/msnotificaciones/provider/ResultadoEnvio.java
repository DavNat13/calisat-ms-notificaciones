package com.califorge.msnotificaciones.provider;

/**
 * Resultado de un intento de despacho contra un proveedor.
 * exito=false con reintentable=true programa REINTENTO con backoff;
 * reintentable=false (4xx de configuracion) va directo a FALLIDO.
 */
public record ResultadoEnvio(
        String proveedor,
        String messageId,
        Integer httpStatus,
        String error,
        boolean exito,
        boolean reintentable) {

    public static ResultadoEnvio exito(String proveedor, String messageId) {
        return new ResultadoEnvio(proveedor, messageId, 200, null, true, true);
    }

    public static ResultadoEnvio fallo(String proveedor, Integer httpStatus, String error, boolean reintentable) {
        return new ResultadoEnvio(proveedor, null, httpStatus, error, false, reintentable);
    }
}
