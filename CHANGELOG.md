# Changelog - calisat-ms-notificaciones

## [2.0.0] - 2026-09-23

### BREAKING CHANGE
- Versión pom.xml incrementada a 2.0.0 (fase B: integración entre microservicios)
- Nuevo componente `CarritoAbandonadoScheduler` añadido al contexto con dependencias `CarritoClient`, `DestinatarioService` y `NotificacionService`; cualquier construcción manual de ese scheduler debe inyectarlas
- El cron de carritos abandonados ingesta notificaciones TRANSACCIONALES (idempotentes por carrito) a partir del directorio de destinatarios (nuevo comportamiento programado diario a las 06:00)

### Added
- Paquete `client` con cliente RestTemplate aislado `CarritoClient` (GET /api/v1/carrito?usuarioSub=&estado=), sin service discovery
- URL base por variable de entorno con default localhost: `CALISAT_CARRITO_URL` (http://localhost:8084)
- `RestTemplateConfig` con el bean `RestTemplate` compartido por los clientes
- Cron `CarritoAbandonadoScheduler`: diario 06:00, revisa carritos ABIERTO sin actualización ≥30 días de los destinatarios activos e ingesta el aviso (clave `carrito-abandonado-{carritoId}`, sin duplicados)
- Degradación elegante: si ms-carrito está caído, la pasada del cron se omite con log y el servicio sigue operando (try/catch best-effort)
- Tests de cliente con `RestTemplate` mockeado (`CarritoClientTest`) y tests del cron (`CarritoAbandonadoSchedulerTest`): aviso, carrito reciente, destinatario inactivo y caída del servicio

## [1.3.0] - 2026-09-23

### Added
- Microservicio calisat-ms-notificaciones con Spring Boot 4.1.0 y Java 21 (puerto 8087)
- Docker Compose con PostgreSQL 15 (`calisat_notificacion`, puerto host 5436) y app Spring Boot
- Entidad JPA Notificacion con estados PENDIENTE/ENVIANDO/ENVIADO/REINTENTO/FALLIDO/CANCELADO/OMITIDO, idempotency_key unica y control de intentos (max 5 con backoff)
- Entidad JPA Plantilla con codigo unico, variables, activa y version (render simple `{{var}}`)
- Entidad JPA Destinatario con azure_sub unico (directorio de destinatarios)
- Entidad JPA Suscripcion con opt-in de campanas por defecto FALSE y token_desuscripcion unico
- Entidad JPA IntentoEnvio como traza por intento de envio (proveedor, messageId, http_status, error)
- Repositorios JPA para las 5 entidades
- NotificacionService con ingesta idempotente de eventos, envio directo, historial paginado, detalle con intentos, mis-notificaciones, reintentado y transiciones de estado
- PlantillaService con CRUD de plantillas (DELETE = baja logica) y render simple de plantillas
- PreferenciaService para consultas y actualizacion opt-in/opt-out de campanas
- DestinatarioService para el directorio de destinatarios
- Poller @Scheduled base para notificaciones PENDIENTE/REINTENTO con proveedor EnvioProvider (implementacion log no-op; SES es fase posterior)
- Endpoints: POST /api/v1/notificaciones/eventos, POST /api/v1/notificaciones/enviar, GET /api/v1/notificaciones, GET /api/v1/notificaciones/{id}, GET /api/v1/notificaciones/mis-notificaciones, POST /api/v1/notificaciones/{id}/reintentar
- Endpoints CRUD /api/v1/plantillas[/{codigo}], GET/PUT /api/v1/preferencias, GET /api/v1/destinatarios
- SecurityConfig con validacion JWT de Azure Entra ID (issuer + audience) y CORS; sin RBAC (solo autenticacion)
- GlobalExceptionHandler con manejo de errores de negocio y validacion
- Tests de servicio (NotificacionServiceTest, PlantillaServiceTest)
- Health check via Spring Actuator

[2.0.0]: https://github.com/DavNat13/calisat-ms-notificaciones/compare/v1.3.0...v2.0.0
[1.3.0]: https://github.com/DavNat13/calisat-ms-notificaciones/releases/tag/v1.3.0
