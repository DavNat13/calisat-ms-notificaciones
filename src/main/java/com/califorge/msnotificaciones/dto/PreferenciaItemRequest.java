package com.califorge.msnotificaciones.dto;

import com.califorge.msnotificaciones.model.Canal;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PreferenciaItemRequest(
        @Schema(description = "Tipo de campana sobre la que se expresa la preferencia.", example = "NOVEDADES")
        @NotBlank(message = "tipoCampana es obligatorio")
        @Size(max = 100, message = "tipoCampana no puede superar 100 caracteres")
        String tipoCampana,

        @Schema(description = "Canal de la preferencia.", example = "EMAIL")
        @NotNull(message = "canal es obligatorio")
        Canal canal,

        @Schema(description = "Opt-in explicito de la campana (por defecto false en el servidor).", example = "true")
        @NotNull(message = "optIn es obligatorio")
        Boolean optIn) {
}
