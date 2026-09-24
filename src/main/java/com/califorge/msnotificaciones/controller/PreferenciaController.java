package com.califorge.msnotificaciones.controller;

import com.califorge.msnotificaciones.dto.PreferenciaRequest;
import com.califorge.msnotificaciones.dto.PreferenciaResponse;
import com.califorge.msnotificaciones.dto.SuscripcionResponse;
import com.califorge.msnotificaciones.service.PreferenciaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Preferencias de campanas del usuario autenticado (opt-in/out).
 * Sin RBAC: el scope por usuario se resuelve con el sub del JWT.
 */
@RestController
@RequestMapping("/api/v1/preferencias")
@Tag(name = "Preferencias", description = "Opt-in/out de campanas del usuario autenticado. Sin RBAC: usuarios autenticados; scope por sub del JWT.")
public class PreferenciaController {

    private final PreferenciaService preferenciaService;

    public PreferenciaController(PreferenciaService preferenciaService) {
        this.preferenciaService = preferenciaService;
    }

    @Operation(summary = "Obtener preferencias", description = "Devuelve las suscripciones a campanas del usuario autenticado (opt_in false por defecto). Requiere JWT; sin roles.")
    @GetMapping
    public ResponseEntity<PreferenciaResponse> obtener(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(PreferenciaResponse.desde(
                preferenciaService.obtener(jwt.getSubject()).stream()
                        .map(SuscripcionResponse::desde)
                        .toList()));
    }

    @Operation(summary = "Actualizar preferencias", description = "Upsert de las preferencias enviadas (opt-in explicito para campanas). Devuelve la lista completa actualizada. Requiere JWT; sin roles.")
    @PutMapping
    public ResponseEntity<PreferenciaResponse> actualizar(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody PreferenciaRequest request) {
        return ResponseEntity.ok(PreferenciaResponse.desde(
                preferenciaService.actualizar(jwt.getSubject(), request).stream()
                        .map(SuscripcionResponse::desde)
                        .toList()));
    }
}
