package com.califorge.msnotificaciones.controller;

import com.califorge.msnotificaciones.dto.DestinatarioResponse;
import com.califorge.msnotificaciones.service.DestinatarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Directorio de destinatarios. Sin RBAC: cualquier usuario autenticado.
 */
@RestController
@RequestMapping("/api/v1/destinatarios")
@Tag(name = "Destinatarios", description = "Directorio de destinatarios conocidos. Sin RBAC: usuarios autenticados.")
public class DestinatarioController {

    private final DestinatarioService destinatarioService;

    public DestinatarioController(DestinatarioService destinatarioService) {
        this.destinatarioService = destinatarioService;
    }

    @Operation(summary = "Listar destinatarios", description = "Devuelve el directorio de destinatarios ordenado por nombre. Requiere JWT; sin roles.")
    @GetMapping
    public ResponseEntity<List<DestinatarioResponse>> listar() {
        return ResponseEntity.ok(destinatarioService.listar().stream()
                .map(DestinatarioResponse::desde)
                .toList());
    }
}
