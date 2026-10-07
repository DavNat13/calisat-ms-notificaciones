package com.califorge.msnotificaciones.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Alta/actualizacion (upsert) de un destinatario en el directorio. La clave
 * unica es azureSub: si ya existe se actualizan los campos informados.
 */
public record DestinatarioAltaRequest(
        @Schema(description = "Sub (oid) de Azure AD. Clave unica del directorio.", example = "e5372bf0-c5e3-4286-887c-79069f209c1f")
        @NotBlank(message = "azureSub es obligatorio")
        @Size(max = 255, message = "azureSub no puede superar 255 caracteres")
        String azureSub,

        @Schema(description = "Email del destinatario.", example = "cliente@ejemplo.com")
        @NotBlank(message = "email es obligatorio")
        @Email(message = "email debe ser valido")
        @Size(max = 320, message = "email no puede superar 320 caracteres")
        String email,

        @Schema(description = "Nombre visible.", example = "Ana Perez")
        @Size(max = 255, message = "nombre no puede superar 255 caracteres")
        String nombre,

        @Schema(description = "Rol informativo (no implica RBAC).", example = "cliente")
        @Size(max = 100, message = "rol no puede superar 100 caracteres")
        String rol,

        @Schema(description = "Si el destinatario es activo. Por defecto true.", example = "true")
        Boolean activo) {
}
