package com.califorge.msnotificaciones.dto;

import com.califorge.msnotificaciones.model.Canal;
import com.califorge.msnotificaciones.model.TipoNotificacion;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record NotificacionEventoRequest(
        @Schema(description = "Canal de despacho. Por defecto EMAIL.", example = "EMAIL")
        Canal canal,

        @Schema(description = "Tipo de notificacion del evento.", example = "SISTEMA")
        TipoNotificacion tipo,

        @Schema(description = "Asunto del mensaje (se omite si se indica plantillaCodigo).", example = "Bienvenida a Calisat")
        @Size(max = 500, message = "asunto no puede superar 500 caracteres")
        String asunto,

        @Schema(description = "Cuerpo en texto plano (se omite si se indica plantillaCodigo).", example = "Hola {{nombre}}, tu cuenta esta lista.")
        String cuerpoTexto,

        @Schema(description = "Cuerpo en HTML opcional.", example = "<p>Hola</p>")
        String cuerpoHtml,

        @Schema(description = "Sub (oid) de Azure AD del destinatario.", example = "e5372bf0-c5e3-4286-887c-79069f209c1f")
        @Size(max = 255, message = "destinatarioSub no puede superar 255 caracteres")
        String destinatarioSub,

        @Schema(description = "Email del destinatario; si se omite se resuelve desde el directorio por sub.", example = "cliente@ejemplo.com")
        @Email(message = "destinatarioEmail debe ser un email valido")
        @Size(max = 320, message = "destinatarioEmail no puede superar 320 caracteres")
        String destinatarioEmail,

        @Schema(description = "Nombre visible del destinatario.", example = "Ana Perez")
        @Size(max = 255, message = "destinatarioNombre no puede superar 255 caracteres")
        String destinatarioNombre,

        @Schema(description = "Codigo de plantilla a renderizar (render simple {{var}}); tiene prioridad sobre asunto/cuerpo.", example = "BIENVENIDA")
        @Size(max = 100, message = "plantillaCodigo no puede superar 100 caracteres")
        String plantillaCodigo,

        @Schema(description = "Payload JSON crudo del evento origen para auditoria.", example = "{\"sku\":\"ANILLAS-001\"}")
        String payloadJson,

        @Schema(description = "Microservicio de origen del evento.", example = "calisat-ms-usuarios")
        @Size(max = 100, message = "origenMs no puede superar 100 caracteres")
        String origenMs,

        @Schema(description = "Id de correlacion del evento origen.", example = "corr-8f2a")
        @Size(max = 100, message = "correlacionId no puede superar 100 caracteres")
        String correlacionId) {
}
