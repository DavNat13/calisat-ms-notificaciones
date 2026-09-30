package com.califorge.msnotificaciones.controller;

import com.califorge.msnotificaciones.dto.NotificacionEnviarRequest;
import com.califorge.msnotificaciones.dto.NotificacionEventoRequest;
import com.califorge.msnotificaciones.dto.NotificacionResponse;
import com.califorge.msnotificaciones.model.EstadoNotificacion;
import com.califorge.msnotificaciones.model.Notificacion;
import com.califorge.msnotificaciones.service.NotificacionDetalleService;
import com.califorge.msnotificaciones.service.NotificacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

/**
 * Endpoints de notificaciones. Sin RBAC: cualquier usuario autenticado con
 * JWT; el scope por usuario se hace con el sub del token.
 */
@RestController
@RequestMapping("/api/v1/notificaciones")
@Tag(name = "Notificaciones", description = "Ingesta idempotente de eventos, envio directo, historial y reintentos. Sin RBAC: cualquier usuario autenticado; el scope por usuario usa el sub del JWT.")
public class NotificacionController {

    /** {id} restringido a UUID canonico: patron disjunto de las rutas literales
     * (p. ej. /mis-notificaciones); un segmento no-UUID responde 404, no 400. */
    static final String UUID_REGEX = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";

    private final NotificacionService notificacionService;
    private final NotificacionDetalleService detalleService;

    public NotificacionController(NotificacionService notificacionService,
                                  NotificacionDetalleService detalleService) {
        this.notificacionService = notificacionService;
        this.detalleService = detalleService;
    }

    /**
     * POST /api/v1/notificaciones/eventos
     * Ingesta idempotente desde otros microservicios via cabecera
     * Idempotency-Key; una clave repetida devuelve la notificacion original.
     */
    @Operation(summary = "Ingesta idempotente de eventos", description = "Registra una notificacion PENDIENTE a partir de un evento de otro microservicio. Si la cabecera Idempotency-Key ya existe devuelve la notificacion original sin duplicar. Requiere JWT; sin roles.")
    @PostMapping("/eventos")
    public ResponseEntity<NotificacionResponse> ingestar(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody NotificacionEventoRequest request) {
        Notificacion notificacion = notificacionService.ingestar(request, idempotencyKey);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .replacePath("/api/v1/notificaciones/{id}")
                .buildAndExpand(notificacion.getId())
                .toUri();
        return ResponseEntity.created(location).body(NotificacionResponse.desde(notificacion));
    }

    /**
     * POST /api/v1/notificaciones/enviar
     * Envio directo transaccional; el destinatario es siempre el propio
     * usuario autenticado (sub del JWT).
     */
    @Operation(summary = "Envio directo", description = "Crea una notificacion PENDIENTE del propio usuario autenticado (sub del JWT). 201 con Location; 400 si la entrada es invalida. Requiere JWT; sin roles.")
    @PostMapping("/enviar")
    public ResponseEntity<NotificacionResponse> enviar(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody NotificacionEnviarRequest request) {
        Notificacion notificacion = notificacionService.enviar(jwt.getSubject(), request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .replacePath("/api/v1/notificaciones/{id}")
                .buildAndExpand(notificacion.getId())
                .toUri();
        return ResponseEntity.created(location).body(NotificacionResponse.desde(notificacion));
    }

    /**
     * GET /api/v1/notificaciones
     * Historial paginado con filtro opcional por estado.
     */
    @Operation(summary = "Historial paginado", description = "Historial de notificaciones ordenado por creacion descendente, con filtro opcional de estado y paginacion. Requiere JWT; sin roles.")
    @GetMapping
    public ResponseEntity<Page<NotificacionResponse>> historial(
            @Parameter(name = "estado", description = "Filtro opcional por estado de la notificacion.")
            @RequestParam(name = "estado", required = false) EstadoNotificacion estado,
            Pageable pageable) {
        Pageable efectivo = NotificacionService.efectivo(pageable);
        Page<NotificacionResponse> pagina = notificacionService.historial(estado, efectivo)
                .map(NotificacionResponse::desde);
        return ResponseEntity.ok(new PageImpl<>(pagina.getContent(), efectivo, pagina.getTotalElements()));
    }

    /**
     * GET /api/v1/notificaciones/mis-notificaciones
     * Notificaciones propias del usuario autenticado (in-app).
     * Ruta literal declarada ANTES de /{id} (intencion documentada).
     */
    @Operation(summary = "Mis notificaciones", description = "Notificaciones propias del usuario autenticado (sub del JWT), paginadas. Requiere JWT; sin roles.")
    @GetMapping("/mis-notificaciones")
    public ResponseEntity<Page<NotificacionResponse>> misNotificaciones(
            @AuthenticationPrincipal Jwt jwt,
            Pageable pageable) {
        Pageable efectivo = NotificacionService.efectivo(pageable);
        Page<NotificacionResponse> pagina = notificacionService
                .misNotificaciones(jwt.getSubject(), efectivo)
                .map(NotificacionResponse::desde);
        return ResponseEntity.ok(new PageImpl<>(pagina.getContent(), efectivo, pagina.getTotalElements()));
    }

    /**
     * GET /api/v1/notificaciones/{id}
     * Detalle de la notificacion con su traza de intentos.
     */
    @Operation(summary = "Detalle de notificacion", description = "Detalle de la notificacion con la traza completa de intentos de envio. 404 si no existe. Requiere JWT; sin roles.")
    @GetMapping("/{id:" + UUID_REGEX + "}")
    public ResponseEntity<NotificacionResponse> detalle(
            @Parameter(name = "id", description = "UUID de la notificacion.", required = true)
            @PathVariable UUID id) {
        return ResponseEntity.ok(detalleService.detalleConIntentos(id));
    }

    /**
     * POST /api/v1/notificaciones/{id}/reintentar
     * Fuerza el reintento de una notificacion FALLIDA/REINTENTO/CANCELADO.
     */
    @Operation(summary = "Forzar reintento", description = "Devuelve una notificacion FALLIDA/REINTENTO/CANCELADO a PENDIENTE con reintento inmediato. 409 si el estado actual no lo permite. Requiere JWT; sin roles.")
    @PostMapping("/{id:" + UUID_REGEX + "}/reintentar")
    public ResponseEntity<NotificacionResponse> reintentar(
            @Parameter(name = "id", description = "UUID de la notificacion.", required = true)
            @PathVariable UUID id) {
        Notificacion notificacion = notificacionService.reintentar(id);
        return ResponseEntity.ok(NotificacionResponse.desde(notificacion));
    }
}
