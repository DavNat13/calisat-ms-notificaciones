package com.califorge.msnotificaciones.dto;

import com.califorge.msnotificaciones.model.ResultadoAuditoria;
import com.califorge.msnotificaciones.model.TipoAuditoria;

import java.time.Instant;

/**
 * Mensaje de auditoria publicado por los listeners de este microservicio a la
 * cola propia auditoria.queue (productor y consumidor en calisat-ms-notificaciones).
 *
 * La clave identifica de forma unica el evento para que una reentrega del
 * broker no duplique filas en la tabla de auditoria.
 *
 * @param tipo entidad auditada (USUARIO / ORDEN / ENVIO)
 * @param evento nombre del evento (usuario.registrado, orden.confirmada, ...)
 * @param referenciaId id de la entidad (identificador de usuario, ordenId, envioId)
 * @param usuarioSub sub Azure del dueño del evento (null en registro)
 * @param destinatarioEmail email al que se intento enviar el correo (null si no hubo)
 * @param resultadoCorreo resultado del intento de correo automatizado
 * @param detalle detalle adicional (motivo de fallo, evento desconocido, ...)
 * @param clave idempotencia unica del evento
 * @param fechaEvento momento en que se proceso el evento
 */
public record AuditoriaMensaje(
        TipoAuditoria tipo,
        String evento,
        String referenciaId,
        String usuarioSub,
        String destinatarioEmail,
        ResultadoAuditoria resultadoCorreo,
        String detalle,
        String clave,
        Instant fechaEvento) {

    /** Construye el mensaje calculando la clave de idempotencia y la fecha. */
    public static AuditoriaMensaje de(TipoAuditoria tipo, String evento, String referenciaId,
                                      String usuarioSub, String destinatarioEmail,
                                      ResultadoAuditoria resultadoCorreo, String detalle) {
        Instant fecha = Instant.now();
        String clave = tipo + ":" + evento + ":" + referenciaId + ":" + fecha;
        return new AuditoriaMensaje(tipo, evento, referenciaId, usuarioSub, destinatarioEmail,
                resultadoCorreo, detalle, clave, fecha);
    }
}
