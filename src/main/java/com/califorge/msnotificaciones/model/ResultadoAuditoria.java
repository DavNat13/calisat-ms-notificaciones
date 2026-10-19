package com.califorge.msnotificaciones.model;

/**
 * Resultado del intento de correo automatizado asociado a un evento auditado.
 */
public enum ResultadoAuditoria {
    /** El correo se despacho correctamente via SMTP. */
    ENVIADO,
    /** El envio SMTP fallo (excepcion de transporte / proveedor). */
    FALLIDO,
    /** El sub del evento no tiene destinatario activo con email resoluble. */
    SIN_DESTINATARIO,
    /** El evento llego con un tipo desconocido: no se envio correo. */
    EVENTO_DESCONOCIDO
}
