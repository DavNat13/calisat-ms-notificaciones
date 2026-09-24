package com.califorge.msnotificaciones.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record PreferenciaResponse(
        @Schema(description = "Preferencias de suscripcion a campanas del usuario autenticado.")
        List<SuscripcionResponse> preferencias) {

    public static PreferenciaResponse desde(List<SuscripcionResponse> preferencias) {
        return new PreferenciaResponse(preferencias == null ? List.of() : preferencias);
    }
}
