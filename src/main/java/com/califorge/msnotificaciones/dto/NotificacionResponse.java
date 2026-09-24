package com.califorge.msnotificaciones.dto;

import com.califorge.msnotificaciones.model.Canal;
import com.califorge.msnotificaciones.model.EstadoNotificacion;
import com.califorge.msnotificaciones.model.Notificacion;
import com.califorge.msnotificaciones.model.TipoNotificacion;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record NotificacionResponse(
        @Schema(description = "Identificador de la notificacion.", example = "7c9e6679-7425-40de-944b-e07fc1f90ae7")
        UUID id,

        @Schema(description = "Sub (oid) de Azure AD del destinatario.", example = "e5372bf0-c5e3-4286-887c-79069f209c1f")
        String destinatarioSub,

        @Schema(description = "Email del destinatario.", example = "cliente@ejemplo.com")
        String destinatarioEmail,

        @Schema(description = "Nombre del destinatario.", example = "Ana Perez")
        String destinatarioNombre,

        @Schema(description = "Canal de despacho.", example = "EMAIL")
        Canal canal,

        @Schema(description = "Tipo de notificacion.", example = "TRANSACCIONAL")
        TipoNotificacion tipo,

        @Schema(description = "Asunto del mensaje.", example = "Tu pedido esta en camino")
        String asunto,

        @Schema(description = "Cuerpo en texto plano.", example = "Hola, tu pedido fue despachado.")
        String cuerpoTexto,

        @Schema(description = "Cuerpo en HTML.", example = "<p>Hola</p>")
        String cuerpoHtml,

        @Schema(description = "Estado actual.", example = "PENDIENTE")
        EstadoNotificacion estado,

        @Schema(description = "Intentos realizados hasta ahora.", example = "0")
        Integer intentos,

        @Schema(description = "Maximo de intentos antes de FALLIDO.", example = "5")
        Integer maxIntentos,

        @Schema(description = "Momento del proximo reintento (null si no esta encolada).")
        LocalDateTime proximoIntentoAt,

        @Schema(description = "Id de mensaje devuelto por el proveedor (SES en fase posterior).", example = "LOG-3f1d")
        String sesMessageId,

        @Schema(description = "Payload JSON crudo del evento origen.", example = "{\"sku\":\"ANILLAS-001\"}")
        String payloadJson,

        @Schema(description = "Microservicio de origen.", example = "calisat-ms-usuarios")
        String origenMs,

        @Schema(description = "Id de correlacion del evento origen.", example = "corr-8f2a")
        String correlacionId,

        @Schema(description = "Fecha de creacion.")
        LocalDateTime fechaCreacion,

        @Schema(description = "Fecha de ultima actualizacion.")
        LocalDateTime fechaActualizacion,

        @Schema(description = "Traza de intentos de envio; vacia en el listado y completa en el detalle.")
        List<IntentoEnvioResponse> intentosEnvio) {

    public static NotificacionResponse desde(Notificacion notificacion) {
        return desde(notificacion, List.of());
    }

    public static NotificacionResponse desde(Notificacion notificacion, List<IntentoEnvioResponse> intentosEnvio) {
        return new NotificacionResponse(
                notificacion.getId(),
                notificacion.getDestinatarioSub(),
                notificacion.getDestinatarioEmail(),
                notificacion.getDestinatarioNombre(),
                notificacion.getCanal(),
                notificacion.getTipo(),
                notificacion.getAsunto(),
                notificacion.getCuerpoTexto(),
                notificacion.getCuerpoHtml(),
                notificacion.getEstado(),
                notificacion.getIntentos(),
                notificacion.getMaxIntentos(),
                notificacion.getProximoIntentoAt(),
                notificacion.getSesMessageId(),
                notificacion.getPayloadJson(),
                notificacion.getOrigenMs(),
                notificacion.getCorrelacionId(),
                notificacion.getFechaCreacion(),
                notificacion.getFechaActualizacion(),
                intentosEnvio == null ? List.of() : intentosEnvio);
    }
}
