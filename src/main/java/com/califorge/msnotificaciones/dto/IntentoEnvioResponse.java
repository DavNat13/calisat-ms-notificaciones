package com.califorge.msnotificaciones.dto;

import com.califorge.msnotificaciones.model.IntentoEnvio;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

public record IntentoEnvioResponse(
        @Schema(description = "Identificador del intento.", example = "3f1d2f6e-8a4b-4c9d-9e2f-1a2b3c4d5e6f")
        UUID id,

        @Schema(description = "Numero de intento (1-based).", example = "1")
        Integer numeroIntento,

        @Schema(description = "Proveedor utilizado.", example = "LOG")
        String proveedor,

        @Schema(description = "Id de mensaje devuelto por el proveedor.", example = "LOG-3f1d")
        String messageId,

        @Schema(description = "Status HTTP del proveedor.", example = "200")
        Integer httpStatus,

        @Schema(description = "Error registrado en el intento (null si fue exitoso).")
        String error,

        @Schema(description = "Si el intento tuvo exito.", example = "true")
        Boolean exito,

        @Schema(description = "Momento del intento.")
        LocalDateTime fechaIntento) {

    public static IntentoEnvioResponse desde(IntentoEnvio intento) {
        return new IntentoEnvioResponse(
                intento.getId(),
                intento.getNumeroIntento(),
                intento.getProveedor(),
                intento.getMessageId(),
                intento.getHttpStatus(),
                intento.getError(),
                intento.getExito(),
                intento.getFechaIntento());
    }
}
