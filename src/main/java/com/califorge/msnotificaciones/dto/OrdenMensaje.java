package com.califorge.msnotificaciones.dto;

/**
 * Mensaje de orden recibido de ms-orden via RabbitMQ.
 * Debe coincidir con la estructura JSON publicada por el productor.
 *
 * @param ordenId identificador de la orden
 * @param usuarioSub sub Azure del dueño de la orden (destinatario)
 * @param evento tipo de evento: ORDEN_CONFIRMADA u ORDEN_CANCELADA
 * @param estado estado de la orden al publicar
 * @param total total de la orden (texto)
 */
public record OrdenMensaje(
        String ordenId,
        String usuarioSub,
        String evento,
        String estado,
        String total) {
}
