package com.califorge.msnotificaciones.model;

/**
 * Maquina de estados de una notificacion.
 * PENDIENTE -> ENVIANDO -> ENVIADO / REINTENTO (backoff, max 5) / FALLIDO.
 * CANCELADO y OMITIDO son estados terminales de baja.
 */
public enum EstadoNotificacion {
    PENDIENTE,
    ENVIANDO,
    ENVIADO,
    REINTENTO,
    FALLIDO,
    CANCELADO,
    OMITIDO
}
