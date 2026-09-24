package com.califorge.msnotificaciones.service;

import com.califorge.msnotificaciones.dto.NotificacionEnviarRequest;
import com.califorge.msnotificaciones.dto.NotificacionEventoRequest;
import com.califorge.msnotificaciones.exception.NotificacionNoEncontradaException;
import com.califorge.msnotificaciones.exception.PlantillaNoEncontradaException;
import com.califorge.msnotificaciones.exception.TransicionEstadoNoPermitidaException;
import com.califorge.msnotificaciones.model.Canal;
import com.califorge.msnotificaciones.model.Destinatario;
import com.califorge.msnotificaciones.model.EstadoNotificacion;
import com.califorge.msnotificaciones.model.IntentoEnvio;
import com.califorge.msnotificaciones.model.Notificacion;
import com.califorge.msnotificaciones.model.Plantilla;
import com.califorge.msnotificaciones.model.TipoNotificacion;
import com.califorge.msnotificaciones.provider.EnvioProvider;
import com.califorge.msnotificaciones.provider.ResultadoEnvio;
import com.califorge.msnotificaciones.repository.DestinatarioRepository;
import com.califorge.msnotificaciones.repository.IntentoEnvioRepository;
import com.califorge.msnotificaciones.repository.NotificacionRepository;
import com.califorge.msnotificaciones.repository.PlantillaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Logica de negocio de notificaciones: ingesta idempotente de eventos,
 * envio directo, historial, transiciones de estado y poller de despacho.
 *
 * Fase actual: solo despacho interno via EnvioProvider (log no-op).
 * NO hay llamadas HTTP a otros microservicios (esa es la fase B).
 */
@Service
@Transactional
public class NotificacionService {

    private static final Logger log = LoggerFactory.getLogger(NotificacionService.class);

    static final long BACKOFF_BASE_SEGUNDOS = 30L;
    static final int LOTE_POLLER = 20;

    private final NotificacionRepository notificacionRepository;
    private final PlantillaRepository plantillaRepository;
    private final DestinatarioRepository destinatarioRepository;
    private final IntentoEnvioRepository intentoEnvioRepository;
    private final EnvioProvider envioProvider;

    public NotificacionService(NotificacionRepository notificacionRepository,
                               PlantillaRepository plantillaRepository,
                               DestinatarioRepository destinatarioRepository,
                               IntentoEnvioRepository intentoEnvioRepository,
                               EnvioProvider envioProvider) {
        this.notificacionRepository = notificacionRepository;
        this.plantillaRepository = plantillaRepository;
        this.destinatarioRepository = destinatarioRepository;
        this.intentoEnvioRepository = intentoEnvioRepository;
        this.envioProvider = envioProvider;
    }

    /**
     * Ingesta idempotente de un evento entrante de otro microservicio.
     * Si la idempotency_key ya existe devuelve la notificacion original
     * sin crear otra (absorbe reenvios del emisor).
     */
    public Notificacion ingestar(NotificacionEventoRequest request, String idempotencyKey) {
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            var existente = notificacionRepository.findByIdempotencyKey(idempotencyKey);
            if (existente.isPresent()) {
                log.info("Ingesta idempotente: clave '{}' ya registrada como notificacion {}",
                        idempotencyKey, existente.get().getId());
                return existente.get();
            }
        }

        Notificacion notificacion = new Notificacion();
        notificacion.setDestinatarioSub(request.destinatarioSub());
        notificacion.setDestinatarioEmail(request.destinatarioEmail());
        notificacion.setDestinatarioNombre(request.destinatarioNombre());
        notificacion.setCanal(request.canal() != null ? request.canal() : Canal.EMAIL);
        notificacion.setTipo(request.tipo() != null ? request.tipo() : TipoNotificacion.TRANSACCIONAL);
        notificacion.setOrigenMs(request.origenMs());
        notificacion.setCorrelacionId(request.correlacionId());
        notificacion.setPayloadJson(request.payloadJson());
        notificacion.setEstado(EstadoNotificacion.PENDIENTE);
        notificacion.setIntentos(0);
        notificacion.setMaxIntentos(Notificacion.MAX_INTENTOS_DEFECTO);
        notificacion.setProximoIntentoAt(LocalDateTime.now());
        notificacion.setIdempotencyKey(idempotencyKey != null && !idempotencyKey.isBlank()
                ? idempotencyKey
                : UUID.randomUUID().toString());

        resolverDestinatarioDesdeDirectorio(notificacion);

        if (request.plantillaCodigo() != null && !request.plantillaCodigo().isBlank()) {
            aplicarPlantilla(notificacion, request.plantillaCodigo());
        } else {
            notificacion.setAsunto(request.asunto());
            notificacion.setCuerpoTexto(request.cuerpoTexto());
            notificacion.setCuerpoHtml(request.cuerpoHtml());
        }

        return notificacionRepository.save(notificacion);
    }

    /**
     * Envio directo (transaccional). El cliente solo puede enviar a si mismo:
     * el destinatario_sub siempre es el sub del JWT autenticado.
     */
    public Notificacion enviar(String azureSub, NotificacionEnviarRequest request) {
        Notificacion notificacion = new Notificacion();
        notificacion.setDestinatarioSub(azureSub);
        notificacion.setDestinatarioEmail(request.destinatarioEmail());
        notificacion.setDestinatarioNombre(request.destinatarioNombre());
        notificacion.setCanal(request.canal() != null ? request.canal() : Canal.EMAIL);
        notificacion.setTipo(TipoNotificacion.TRANSACCIONAL);
        notificacion.setAsunto(request.asunto());
        notificacion.setCuerpoTexto(request.cuerpoTexto());
        notificacion.setCuerpoHtml(request.cuerpoHtml());
        notificacion.setOrigenMs("api-directa");
        notificacion.setEstado(EstadoNotificacion.PENDIENTE);
        notificacion.setIntentos(0);
        notificacion.setMaxIntentos(Notificacion.MAX_INTENTOS_DEFECTO);
        notificacion.setProximoIntentoAt(LocalDateTime.now());
        notificacion.setIdempotencyKey(UUID.randomUUID().toString());

        resolverDestinatarioDesdeDirectorio(notificacion);

        return notificacionRepository.save(notificacion);
    }

    /**
     * Detalle de una notificacion por id; 404 si no existe.
     */
    @Transactional(readOnly = true)
    public Notificacion obtenerPorId(UUID id) {
        return notificacionRepository.findById(id)
                .orElseThrow(() -> new NotificacionNoEncontradaException(String.valueOf(id)));
    }

    /**
     * Historial paginado con filtro opcional de estado (ordenado por creacion desc).
     */
    @Transactional(readOnly = true)
    public Page<Notificacion> historial(EstadoNotificacion estado, Pageable pageable) {
        if (estado != null) {
            return notificacionRepository.findByEstadoOrderByFechaCreacionDesc(estado, pageable);
        }
        return notificacionRepository.findAllByOrderByFechaCreacionDesc(pageable);
    }

    /**
     * Notificaciones propias del usuario autenticado (in-app).
     */
    @Transactional(readOnly = true)
    public Page<Notificacion> misNotificaciones(String azureSub, Pageable pageable) {
        return notificacionRepository.findByDestinatarioSub(azureSub, pageable);
    }

    /**
     * Fuerza el reintento de una notificacion FALLIDA/REINTENTO/CANCELADO:
     * vuelve a PENDIENTE con proximo intento inmediato.
     */
    public Notificacion reintentar(UUID id) {
        Notificacion notificacion = obtenerPorId(id);
        EstadoNotificacion actual = notificacion.getEstado();
        if (actual != EstadoNotificacion.FALLIDO
                && actual != EstadoNotificacion.REINTENTO
                && actual != EstadoNotificacion.CANCELADO) {
            throw new TransicionEstadoNoPermitidaException(
                    String.valueOf(id), String.valueOf(actual), "PENDIENTE");
        }
        notificacion.setEstado(EstadoNotificacion.PENDIENTE);
        notificacion.setProximoIntentoAt(LocalDateTime.now());
        return notificacionRepository.save(notificacion);
    }

    /**
     * Cancela una notificacion aun no despachada (PENDIENTE/REINTENTO).
     */
    public Notificacion cancelar(UUID id) {
        Notificacion notificacion = obtenerPorId(id);
        EstadoNotificacion actual = notificacion.getEstado();
        if (actual != EstadoNotificacion.PENDIENTE && actual != EstadoNotificacion.REINTENTO) {
            throw new TransicionEstadoNoPermitidaException(
                    String.valueOf(id), String.valueOf(actual), "CANCELADO");
        }
        notificacion.setEstado(EstadoNotificacion.CANCELADO);
        return notificacionRepository.save(notificacion);
    }

    /**
     * Marca PENDIENTE -> OMITIDO (sin opt-in para campanas).
     */
    public Notificacion omitir(UUID id) {
        Notificacion notificacion = obtenerPorId(id);
        if (notificacion.getEstado() != EstadoNotificacion.PENDIENTE) {
            throw new TransicionEstadoNoPermitidaException(
                    String.valueOf(id), String.valueOf(notificacion.getEstado()), "OMITIDO");
        }
        notificacion.setEstado(EstadoNotificacion.OMITIDO);
        return notificacionRepository.save(notificacion);
    }

    /**
     * Procesa el lote de notificaciones PENDIENTE/REINTENTO cuyo
     * proximo_intento_at ya vencio (invocado por el poller @Scheduled).
     */
    public int procesarPendientes() {
        LocalDateTime ahora = LocalDateTime.now();
        List<Notificacion> candidatas = notificacionRepository
                .findByEstadoIn(List.of(EstadoNotificacion.PENDIENTE, EstadoNotificacion.REINTENTO))
                .stream()
                .filter(n -> n.getProximoIntentoAt() == null || !n.getProximoIntentoAt().isAfter(ahora))
                .limit(LOTE_POLLER)
                .toList();

        int procesadas = 0;
        for (Notificacion notificacion : candidatas) {
            procesar(notificacion);
            procesadas++;
        }
        if (procesadas > 0) {
            log.info("Poller de notificaciones: {} notificacion(es) despachada(s)", procesadas);
        }
        return procesadas;
    }

    /**
     * Transicion atoma en memoria: PENDIENTE/REINTENTO -> ENVIANDO,
     * despacho via proveedor, traza en intento_envio y transicion final
     * ENVIADO / REINTENTO (backoff 30s * 2^n) / FALLIDO.
     */
    public Notificacion procesar(Notificacion notificacion) {
        EstadoNotificacion actual = notificacion.getEstado();
        if (actual != EstadoNotificacion.PENDIENTE && actual != EstadoNotificacion.REINTENTO) {
            throw new TransicionEstadoNoPermitidaException(
                    String.valueOf(notificacion.getId()), String.valueOf(actual), "ENVIANDO");
        }

        notificacion.setEstado(EstadoNotificacion.ENVIANDO);
        notificacionRepository.save(notificacion);

        ResultadoEnvio resultado;
        try {
            resultado = envioProvider.enviar(notificacion);
        } catch (RuntimeException ex) {
            resultado = ResultadoEnvio.fallo("DESPACHO", null, ex.getMessage(), true);
        }

        int numeroIntento = notificacion.getIntentos() + 1;
        IntentoEnvio intento = new IntentoEnvio(
                notificacion,
                numeroIntento,
                resultado.proveedor(),
                resultado.messageId(),
                resultado.httpStatus(),
                resultado.error(),
                resultado.exito());
        intentoEnvioRepository.save(intento);
        notificacion.setIntentos(numeroIntento);

        if (resultado.exito()) {
            notificacion.setEstado(EstadoNotificacion.ENVIADO);
            notificacion.setSesMessageId(resultado.messageId());
        } else if (!resultado.reintentable() || numeroIntento >= notificacion.getMaxIntentos()) {
            notificacion.setEstado(EstadoNotificacion.FALLIDO);
        } else {
            long espera = BACKOFF_BASE_SEGUNDOS * (1L << (numeroIntento - 1));
            notificacion.setEstado(EstadoNotificacion.REINTENTO);
            notificacion.setProximoIntentoAt(LocalDateTime.now().plusSeconds(espera));
        }

        return notificacionRepository.save(notificacion);
    }

    private void resolverDestinatarioDesdeDirectorio(Notificacion notificacion) {
        if (notificacion.getDestinatarioSub() == null || notificacion.getDestinatarioSub().isBlank()) {
            return;
        }
        if (notificacion.getDestinatarioEmail() != null && notificacion.getDestinatarioNombre() != null) {
            return;
        }
        Destinatario destinatario = destinatarioRepository
                .findByAzureSub(notificacion.getDestinatarioSub())
                .orElse(null);
        if (destinatario == null) {
            return;
        }
        if (notificacion.getDestinatarioEmail() == null) {
            notificacion.setDestinatarioEmail(destinatario.getEmail());
        }
        if (notificacion.getDestinatarioNombre() == null) {
            notificacion.setDestinatarioNombre(destinatario.getNombre());
        }
    }

    private void aplicarPlantilla(Notificacion notificacion, String codigoPlantilla) {
        Plantilla plantilla = plantillaRepository.findByCodigo(codigoPlantilla)
                .orElseThrow(() -> new PlantillaNoEncontradaException(codigoPlantilla));

        Map<String, String> variables = Map.of(
                "nombre", notificacion.getDestinatarioNombre() != null ? notificacion.getDestinatarioNombre() : "",
                "email", notificacion.getDestinatarioEmail() != null ? notificacion.getDestinatarioEmail() : "");

        notificacion.setAsunto(renderizar(plantilla.getAsunto(), variables));
        notificacion.setCuerpoTexto(renderizar(plantilla.getCuerpoTexto(), variables));
        notificacion.setCuerpoHtml(renderizar(plantilla.getCuerpoHtml(), variables));
    }

    /**
     * Render simple de plantillas: sustituye {{clave}} por el valor.
     * Sin Thymeleaf ni motor de plantillas (contratado para v1).
     */
    public static String renderizar(String texto, Map<String, String> variables) {
        if (texto == null) {
            return null;
        }
        String resultado = texto;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            resultado = resultado.replace("{{" + entry.getKey() + "}}",
                    entry.getValue() != null ? entry.getValue() : "");
        }
        return resultado;
    }

    /** Normaliza un Pageable sin paginar a una pagina concreta (evita Unpaged en JSON). */
    public static Pageable efectivo(Pageable pageable) {
        return pageable != null && pageable.isPaged() ? pageable : PageRequest.of(0, 20);
    }
}
