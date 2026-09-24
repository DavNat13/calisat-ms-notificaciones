package com.califorge.msnotificaciones.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PlantillaRequest(
        @Schema(description = "Codigo unico de la plantilla.", example = "BIENVENIDA")
        @NotBlank(message = "codigo es obligatorio")
        @Size(max = 100, message = "codigo no puede superar 100 caracteres")
        String codigo,

        @Schema(description = "Asunto con placeholders {{var}}.", example = "Bienvenida a Calisat, {{nombre}}")
        @Size(max = 500, message = "asunto no puede superar 500 caracteres")
        String asunto,

        @Schema(description = "Cuerpo en texto plano con placeholders {{var}}.", example = "Hola {{nombre}}, tu cuenta esta lista.")
        String cuerpoTexto,

        @Schema(description = "Cuerpo en HTML con placeholders {{var}}.", example = "<p>Hola {{nombre}}</p>")
        String cuerpoHtml,

        @Schema(description = "Variables de la plantilla en formato JSON.", example = "[\"nombre\",\"email\"]")
        String variables,

        @Schema(description = "Si la plantilla esta activa. Por defecto true.", example = "true")
        Boolean activa) {
}
