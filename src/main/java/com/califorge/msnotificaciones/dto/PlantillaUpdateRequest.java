package com.califorge.msnotificaciones.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

public record PlantillaUpdateRequest(
        @Schema(description = "Asunto con placeholders {{var}}.", example = "Bienvenida a Calisat, {{nombre}}")
        @Size(max = 500, message = "asunto no puede superar 500 caracteres")
        String asunto,

        @Schema(description = "Cuerpo en texto plano con placeholders {{var}}.", example = "Hola {{nombre}}, tu cuenta esta lista.")
        String cuerpoTexto,

        @Schema(description = "Cuerpo en HTML con placeholders {{var}}.", example = "<p>Hola {{nombre}}</p>")
        String cuerpoHtml,

        @Schema(description = "Variables de la plantilla en formato JSON.", example = "[\"nombre\",\"email\"]")
        String variables,

        @Schema(description = "Flag de activa; si se omite no se modifica.", example = "true")
        Boolean activa) {
}
