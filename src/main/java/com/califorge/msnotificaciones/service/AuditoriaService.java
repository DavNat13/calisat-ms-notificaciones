package com.califorge.msnotificaciones.service;

import com.califorge.msnotificaciones.dto.AuditoriaMensaje;
import com.califorge.msnotificaciones.model.AuditoriaEvento;
import com.califorge.msnotificaciones.model.ResultadoAuditoria;
import com.califorge.msnotificaciones.model.TipoAuditoria;
import com.califorge.msnotificaciones.repository.AuditoriaEventoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Persistencia y consulta de la auditoria de eventos de usuario/orden/envio
 * consumida desde la cola propia auditoria.queue.
 */
@Service
public class AuditoriaService {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaService.class);

    private final AuditoriaEventoRepository repository;

    public AuditoriaService(AuditoriaEventoRepository repository) {
        this.repository = repository;
    }

    /**
     * Registra el evento de auditoria. Idempotente por clave: una reentrega
     * del broker no duplica filas.
     */
    public AuditoriaEvento registrar(AuditoriaMensaje mensaje) {
        if (mensaje == null) {
            throw new IllegalArgumentException("Mensaje de auditoria nulo");
        }
        if (repository.findByClave(mensaje.clave()).isPresent()) {
            log.info("Auditoria duplicada ignorada: clave={}", mensaje.clave());
            return null;
        }
        AuditoriaEvento evento = new AuditoriaEvento();
        evento.setClave(mensaje.clave());
        evento.setTipo(mensaje.tipo());
        evento.setEvento(mensaje.evento());
        evento.setReferenciaId(mensaje.referenciaId());
        evento.setUsuarioSub(mensaje.usuarioSub());
        evento.setDestinatarioEmail(mensaje.destinatarioEmail());
        evento.setResultadoCorreo(mensaje.resultadoCorreo());
        evento.setDetalle(recortar(mensaje.detalle()));
        evento.setFechaEvento(mensaje.fechaEvento());
        AuditoriaEvento guardado = repository.save(evento);
        log.info("Auditoria registrada: tipo={} evento={} referencia={} resultado={}",
                guardado.getTipo(), guardado.getEvento(), guardado.getReferenciaId(),
                guardado.getResultadoCorreo());
        return guardado;
    }

    /** Historial paginado con filtros opcionales por tipo y resultado del correo. */
    public Page<AuditoriaEvento> historial(TipoAuditoria tipo, ResultadoAuditoria resultado, Pageable pageable) {
        Pageable efectivo = efectivo(pageable);
        if (tipo != null && resultado != null) {
            return repository.findByTipoAndResultadoCorreoOrderByFechaEventoDesc(tipo, resultado, efectivo);
        }
        if (tipo != null) {
            return repository.findByTipoOrderByFechaEventoDesc(tipo, efectivo);
        }
        if (resultado != null) {
            return repository.findByResultadoCorreoOrderByFechaEventoDesc(resultado, efectivo);
        }
        return repository.findAllByOrderByFechaEventoDesc(efectivo);
    }

    /** Normaliza un Pageable sin paginar a una pagina concreta (evita Unpaged en JSON). */
    public static Pageable efectivo(Pageable pageable) {
        return pageable != null && pageable.isPaged() ? pageable : PageRequest.of(0, 20);
    }

    /** Recorta el detalle al maximo de la columna (500) sin partir palabras. */
    private static String recortar(String detalle) {
        if (detalle == null) {
            return null;
        }
        String limpio = detalle.strip();
        return limpio.length() > 500 ? limpio.substring(0, 500) : limpio;
    }
}
