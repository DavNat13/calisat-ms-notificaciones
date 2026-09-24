package com.califorge.msnotificaciones.dto;

/**
 * Mensaje de envio recibido de ms-envios via RabbitMQ.
 * Debe coincidir con la estructura JSON publicada por el productor.
 *
 * @param envioId identificador del envio
 * @param ordenId identificador de la orden asociada
 * @param usuarioSub sub Azure del destinatario
 * @param numeroGuia numero de guia del envio (prefijo CAL-)
 * @param transportista transportista asignado (puede ser nulo)
 * @param evento tipo de evento: ENVIO_DESPACHADO u ENVIO_ENTREGADO
 * @param estado estado del envio al publicar
 */
public record EnvioMensaje(
        String envioId,
        String ordenId,
        String usuarioSub,
        String numeroGuia,
        String transportista,
        String evento,
        String estado) {
}
