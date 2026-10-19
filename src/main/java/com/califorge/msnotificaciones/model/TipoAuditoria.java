package com.califorge.msnotificaciones.model;

/**
 * Tipo de entidad auditada por la cola de auditoria interna
 * (usuario registrado, orden confirmada/cancelada, envio despachado/entregado).
 */
public enum TipoAuditoria {
    USUARIO,
    ORDEN,
    ENVIO
}
