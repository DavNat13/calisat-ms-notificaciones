package com.califorge.msnotificaciones.model;

/**
 * Tipo de notificacion: transaccional (siempre ON), campana (requiere
 * opt-in del destinatario) o sistema (interno entre microservicios).
 */
public enum TipoNotificacion {
    TRANSACCIONAL,
    CAMPANA,
    SISTEMA
}
