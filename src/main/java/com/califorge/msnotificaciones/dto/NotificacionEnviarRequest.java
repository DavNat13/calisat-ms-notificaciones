package com.califorge.msnotificaciones.dto;

import com.califorge.msnotificaciones.model.Canal;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NotificacionEnviarRequest(
        @Schema(description = "Canal de despacho. Por defecto EMAIL.", example = "EMAIL")
        Canal canal,

        @Schema(description = "Asunto del mensaje.", example = "Tu pedido esta en camino")
        @NotBlank(message = "asunto es obligatorio")
        @Size(max = 500, message = "asunto no puede superar 500 caracteres")
        String asunto,

        @Schema(description = "Cuerpo en texto plano.", example = "Hola, tu pedido fue despachado.")
        @NotBlank(message = "cuerpoTexto es obligatorio")
        String cuerpoTexto,

        @Schema(description = "Cuerpo en HTML opcional.", example = "<p>Hola</p>")
        String cuerpoHtml,

        @Schema(description = "Email del destinatario; si se omite se resuelve desde el directorio por el sub autenticado.", example = "yo@ejemplo.com")
        @Email(message = "destinatarioEmail debe ser un email valido")
        @Size(max = 320, message = "destinatarioEmail no puede superar 320 caracteres")
        String destinatarioEmail,

        @Schema(description = "Nombre visible del destinatario.", example = "Ana Perez")
        @Size(max = 255, message = "destinatarioNombre no puede superar 255 caracteres")
        String destinatarioNombre) {
}
