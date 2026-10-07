package com.califorge.msnotificaciones.controller;

import com.califorge.msnotificaciones.dto.DestinatarioAltaRequest;
import com.califorge.msnotificaciones.dto.DestinatarioResponse;
import com.califorge.msnotificaciones.model.Destinatario;
import com.califorge.msnotificaciones.service.DestinatarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
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

    /**
     * POST /api/v1/destinatarios
     * Alta en el directorio (upsert por azureSub). Sin esta tabla,
     * OrdenListener/EnvioListener no resuelven a quien enviar el correo.
     */
    @Operation(summary = "Alta de destinatario", description = "Crea o actualiza (upsert por azureSub) un destinatario del directorio. 201 con Location; 400 si la entrada es invalida. Requiere JWT; sin roles.")
    @PostMapping
    public ResponseEntity<DestinatarioResponse> alta(
            @Valid @RequestBody DestinatarioAltaRequest request) {
        Destinatario guardado = destinatarioService.alta(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .replacePath("/api/v1/destinatarios/{id}")
                .buildAndExpand(guardado.getId())
                .toUri();
        return ResponseEntity.created(location).body(DestinatarioResponse.desde(guardado));
    }
}
