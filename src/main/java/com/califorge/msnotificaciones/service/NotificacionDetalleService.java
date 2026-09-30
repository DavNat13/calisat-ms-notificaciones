package com.califorge.msnotificaciones.service;

import com.califorge.msnotificaciones.dto.IntentoEnvioResponse;
import com.califorge.msnotificaciones.dto.NotificacionResponse;
import com.califorge.msnotificaciones.model.Notificacion;
import com.califorge.msnotificaciones.repository.IntentoEnvioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Lectura del detalle de una notificacion junto con su traza de intentos de
 * envio. La composicion notificacion + intentos vive en la capa de servicio
 * (no en el controller) para mantener NotificacionController por debajo del
 * limite de 150 lineas por archivo.
 */
@Service
public class NotificacionDetalleService {

    private final NotificacionService notificacionService;
    private final IntentoEnvioRepository intentoEnvioRepository;

    public NotificacionDetalleService(NotificacionService notificacionService,
                                      IntentoEnvioRepository intentoEnvioRepository) {
        this.notificacionService = notificacionService;
        this.intentoEnvioRepository = intentoEnvioRepository;
    }

    /**
     * Detalle de una notificacion existente (404 si no existe) con la traza
     * completa de intentos de envio ordenada por numero de intento asc.
     */
    @Transactional(readOnly = true)
    public NotificacionResponse detalleConIntentos(UUID id) {
        Notificacion notificacion = notificacionService.obtenerPorId(id);
        List<IntentoEnvioResponse> intentos = intentoEnvioRepository
                .findByNotificacionIdOrderByNumeroIntentoAsc(id)
                .stream()
                .map(IntentoEnvioResponse::desde)
                .toList();
        return NotificacionResponse.desde(notificacion, intentos);
    }
}
