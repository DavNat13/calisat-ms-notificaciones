package com.califorge.msnotificaciones.dto;

import com.califorge.msnotificaciones.model.Destinatario;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record DestinatarioResponse(
        @Schema(description = "Identificador del destinatario.", example = "7c9e6679-7425-40de-944b-e07fc1f90ae7")
        UUID id,

        @Schema(description = "Sub (oid) de Azure AD.", example = "e5372bf0-c5e3-4286-887c-79069f209c1f")
        String azureSub,

        @Schema(description = "Email del destinatario.", example = "cliente@ejemplo.com")
        String email,

        @Schema(description = "Nombre visible.", example = "Ana Perez")
        String nombre,

        @Schema(description = "Rol registrado en el directorio (dato informativo; no implica RBAC).", example = "cliente")
        String rol,

        @Schema(description = "Si el destinatario es activo (false = dado de baja, p.ej. bounce).", example = "true")
        Boolean activo) {

    public static DestinatarioResponse desde(Destinatario destinatario) {
        return new DestinatarioResponse(
                destinatario.getId(),
                destinatario.getAzureSub(),
                destinatario.getEmail(),
                destinatario.getNombre(),
                destinatario.getRol(),
                destinatario.getActivo());
    }
}
