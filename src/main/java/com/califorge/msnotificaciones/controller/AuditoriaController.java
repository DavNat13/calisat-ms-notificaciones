package com.califorge.msnotificaciones.controller;

import com.califorge.msnotificaciones.dto.AuditoriaResponse;
import com.califorge.msnotificaciones.model.ResultadoAuditoria;
import com.califorge.msnotificaciones.model.TipoAuditoria;
import com.califorge.msnotificaciones.service.AuditoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Historial de la auditoria interna (eventos de usuario/orden/envio y
 * resultado de los correos automatizados). Solo ROLE_ADMINISTRADOR.
 */
@RestController
@RequestMapping("/api/v1/auditoria")
@Tag(name = "Auditoria", description = "Historial de eventos de usuario/orden/envio auditados por la cola interna auditoria.queue y resultado del correo automatizado. Solo ROLE_ADMINISTRADOR.")
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    public AuditoriaController(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    /**
     * GET /api/v1/auditoria
     * Historial paginado con filtros opcionales por tipo y resultado del correo.
     */
    @Operation(summary = "Historial de auditoria", description = "Eventos de auditoria (usuario/orden/envio) ordenados por fecha de evento descendente, con filtros opcionales tipo y resultado. Requiere ROLE_ADMINISTRADOR.")
    @GetMapping
    public ResponseEntity<Page<AuditoriaResponse>> historial(
            @Parameter(name = "tipo", description = "Filtro opcional por tipo de entidad (USUARIO, ORDEN, ENVIO).")
            @RequestParam(name = "tipo", required = false) TipoAuditoria tipo,
            @Parameter(name = "resultado", description = "Filtro opcional por resultado del correo (ENVIADO, FALLIDO, SIN_DESTINATARIO, EVENTO_DESCONOCIDO).")
            @RequestParam(name = "resultado", required = false) ResultadoAuditoria resultado,
            Pageable pageable) {
        Pageable efectivo = AuditoriaService.efectivo(pageable);
        Page<AuditoriaResponse> pagina = auditoriaService.historial(tipo, resultado, efectivo)
                .map(AuditoriaResponse::desde);
        return ResponseEntity.ok(new PageImpl<>(pagina.getContent(), efectivo, pagina.getTotalElements()));
    }
}
