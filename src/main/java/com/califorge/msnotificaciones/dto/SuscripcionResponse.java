package com.califorge.msnotificaciones.dto;

import com.califorge.msnotificaciones.model.Canal;
import com.califorge.msnotificaciones.model.Suscripcion;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record SuscripcionResponse(
        @Schema(description = "Identificador de la suscripcion.", example = "7c9e6679-7425-40de-944b-e07fc1f90ae7")
        UUID id,

        @Schema(description = "Tipo de campana.", example = "NOVEDADES")
        String tipoCampana,

        @Schema(description = "Canal de la suscripcion.", example = "EMAIL")
        Canal canal,

        @Schema(description = "Opt-in explicito (false por defecto).", example = "false")
        Boolean optIn,

        @Schema(description = "Token de baja en 1 clic desde el email.", example = "3f1d2f6e-8a4b-4c9d-9e2f-1a2b3c4d5e6f")
        String tokenDesuscripcion) {

    public static SuscripcionResponse desde(Suscripcion suscripcion) {
        return new SuscripcionResponse(
                suscripcion.getId(),
                suscripcion.getTipoCampana(),
                suscripcion.getCanal(),
                suscripcion.getOptIn(),
                suscripcion.getTokenDesuscripcion());
    }
}
