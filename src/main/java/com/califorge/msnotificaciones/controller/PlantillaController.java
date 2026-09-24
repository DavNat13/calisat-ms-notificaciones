package com.califorge.msnotificaciones.controller;

import com.califorge.msnotificaciones.dto.PlantillaRequest;
import com.califorge.msnotificaciones.dto.PlantillaResponse;
import com.califorge.msnotificaciones.dto.PlantillaUpdateRequest;
import com.califorge.msnotificaciones.model.Plantilla;
import com.califorge.msnotificaciones.service.PlantillaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * CRUD de plantillas. Sin RBAC: cualquier usuario autenticado;
 * DELETE es baja logica (activa=false), nunca borra fisicamente.
 */
@RestController
@RequestMapping("/api/v1/plantillas")
@Tag(name = "Plantillas", description = "CRUD de plantillas de mensaje con render simple {{var}}. DELETE es baja logica. Sin RBAC: usuarios autenticados.")
public class PlantillaController {

    private final PlantillaService plantillaService;

    public PlantillaController(PlantillaService plantillaService) {
        this.plantillaService = plantillaService;
    }

    @Operation(summary = "Listar plantillas", description = "Devuelve todas las plantillas registradas. Requiere JWT; sin roles.")
    @GetMapping
    public ResponseEntity<List<PlantillaResponse>> listar() {
        return ResponseEntity.ok(plantillaService.listar().stream()
                .map(PlantillaResponse::desde)
                .toList());
    }

    @Operation(summary = "Obtener plantilla", description = "Devuelve una plantilla por su codigo. 404 si no existe. Requiere JWT; sin roles.")
    @GetMapping("/{codigo}")
    public ResponseEntity<PlantillaResponse> obtener(
            @Parameter(name = "codigo", description = "Codigo de la plantilla.", required = true)
            @PathVariable String codigo) {
        return ResponseEntity.ok(PlantillaResponse.desde(plantillaService.obtener(codigo)));
    }

    @Operation(summary = "Crear plantilla", description = "Crea una plantilla nueva (version 1). 201 con Location; 400 si el codigo ya existe o la entrada es invalida. Requiere JWT; sin roles.")
    @PostMapping
    public ResponseEntity<PlantillaResponse> crear(@Valid @RequestBody PlantillaRequest request) {
        Plantilla plantilla = plantillaService.crear(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{codigo}")
                .buildAndExpand(plantilla.getCodigo())
                .toUri();
        return ResponseEntity.created(location).body(PlantillaResponse.desde(plantilla));
    }

    @Operation(summary = "Actualizar plantilla", description = "Actualiza una plantilla existente e incrementa su version. 404 si el codigo no existe. Requiere JWT; sin roles.")
    @PutMapping("/{codigo}")
    public ResponseEntity<PlantillaResponse> actualizar(
            @Parameter(name = "codigo", description = "Codigo de la plantilla.", required = true)
            @PathVariable String codigo,
            @Valid @RequestBody PlantillaUpdateRequest request) {
        return ResponseEntity.ok(PlantillaResponse.desde(plantillaService.actualizar(codigo, request)));
    }

    @Operation(summary = "Eliminar plantilla (baja logica)", description = "Marca la plantilla como inactiva (activa=false); no borra el registro. 204 en exito; 404 si el codigo no existe. Requiere JWT; sin roles.")
    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void> eliminar(
            @Parameter(name = "codigo", description = "Codigo de la plantilla.", required = true)
            @PathVariable String codigo) {
        plantillaService.eliminar(codigo);
        return ResponseEntity.noContent().build();
    }
}
