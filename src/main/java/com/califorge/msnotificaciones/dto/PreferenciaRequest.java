package com.califorge.msnotificaciones.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PreferenciaRequest(
        @Schema(description = "Preferencias de campana a registrar/actualizar.")
        @NotNull(message = "preferencias es obligatoria")
        @Valid
        List<PreferenciaItemRequest> preferencias) {
}
