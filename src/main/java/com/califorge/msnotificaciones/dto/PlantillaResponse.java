package com.califorge.msnotificaciones.dto;

import com.califorge.msnotificaciones.model.Plantilla;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

public record PlantillaResponse(
        @Schema(description = "Identificador de la plantilla.", example = "7c9e6679-7425-40de-944b-e07fc1f90ae7")
        UUID id,

        @Schema(description = "Codigo unico de la plantilla.", example = "BIENVENIDA")
        String codigo,

        @Schema(description = "Asunto con placeholders {{var}}.", example = "Bienvenida a Calisat, {{nombre}}")
        String asunto,

        @Schema(description = "Cuerpo en texto plano.", example = "Hola {{nombre}}")
        String cuerpoTexto,

        @Schema(description = "Cuerpo en HTML.", example = "<p>Hola {{nombre}}</p>")
        String cuerpoHtml,

        @Schema(description = "Variables de la plantilla en formato JSON.", example = "[\"nombre\"]")
        String variables,

        @Schema(description = "Si la plantilla esta activa.", example = "true")
        Boolean activa,

        @Schema(description = "Version de la plantilla (se incrementa en cada actualizacion).", example = "1")
        Integer version,

        @Schema(description = "Fecha de ultima actualizacion.")
        LocalDateTime fechaActualizacion) {

    public static PlantillaResponse desde(Plantilla plantilla) {
        return new PlantillaResponse(
                plantilla.getId(),
                plantilla.getCodigo(),
                plantilla.getAsunto(),
                plantilla.getCuerpoTexto(),
                plantilla.getCuerpoHtml(),
                plantilla.getVariables(),
                plantilla.getActiva(),
                plantilla.getVersion(),
                plantilla.getFechaActualizacion());
    }
}
