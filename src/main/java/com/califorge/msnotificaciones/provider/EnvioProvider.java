package com.califorge.msnotificaciones.provider;

import com.califorge.msnotificaciones.model.Notificacion;

/**
 * Abstraccion del proveedor de despacho de notificaciones.
 * Fase A: implementacion log/no-op (LogEnvioProvider). AWS SES v2 llegara
 * en la fase de integracion, sin cambiar el resto del servicio.
 */
public interface EnvioProvider {

    ResultadoEnvio enviar(Notificacion notificacion);
}
