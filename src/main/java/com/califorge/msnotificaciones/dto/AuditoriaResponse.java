package com.califorge.msnotificaciones.dto;

import com.califorge.msnotificaciones.model.AuditoriaEvento;
import com.califorge.msnotificaciones.model.ResultadoAuditoria;
import com.califorge.msnotificaciones.model.TipoAuditoria;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record AuditoriaResponse(

        @Schema(description = "Identificador del evento de auditoria.", example = "7c9e6679-7425-40de-944b-e07fc1f90ae7")
        UUID id,

        @Schema(description = "Entidad auditada.", example = "ORDEN")
        TipoAuditoria tipo,

        @Schema(description = "Nombre del evento.", example = "orden.confirmada")
        String evento,

        @Schema(description = "Id de la entidad auditada.", example = "orden-123")
        String referenciaId,

        @Schema(description = "Sub Azure del dueño del evento.", example = "e5372bf0-c5e3-4286-887c-79069f209c1f")
        String usuarioSub,

        @Schema(description = "Email al que se intento enviar el correo.", example = "cliente@ejemplo.com")
        String destinatarioEmail,

        @Schema(description = "Resultado del correo automatizado.", example = "ENVIADO")
        ResultadoAuditoria resultadoCorreo,

        @Schema(description = "Detalle adicional del intento.", example = "SMTP timeout")
        String detalle,

        @Schema(description = "Momento en que se proceso el evento.")
        Instant fechaEvento,

        @Schema(description = "Momento en que se registro en la auditoria.")
        LocalDateTime fechaRegistro) {

    public static AuditoriaResponse desde(AuditoriaEvento evento) {
        return new AuditoriaResponse(
                evento.getId(),
                evento.getTipo(),
                evento.getEvento(),
                evento.getReferenciaId(),
                evento.getUsuarioSub(),
                evento.getDestinatarioEmail(),
                evento.getResultadoCorreo(),
                evento.getDetalle(),
                evento.getFechaEvento(),
                evento.getFechaRegistro());
    }
}
