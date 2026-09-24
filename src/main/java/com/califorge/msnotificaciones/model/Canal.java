package com.califorge.msnotificaciones.model;

/**
 * Canal de despacho de una notificacion. En la fase actual solo se
 * implementa el proveedor log (EMAIL llegara con AWS SES en fase posterior).
 */
public enum Canal {
    EMAIL,
    SMS,
    PUSH,
    IN_APP
}
