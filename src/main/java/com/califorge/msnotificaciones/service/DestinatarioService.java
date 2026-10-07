package com.califorge.msnotificaciones.service;

import com.califorge.msnotificaciones.dto.DestinatarioAltaRequest;
import com.califorge.msnotificaciones.model.Destinatario;
import com.califorge.msnotificaciones.repository.DestinatarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Directorio de destinatarios: alta/actualizacion por azureSub y consulta.
 * De aqui resuelven email OrdenListener, EnvioListener y el pipeline
 * /eventos y /enviar de notificaciones.
 */
@Service
@Transactional(readOnly = true)
public class DestinatarioService {

    private final DestinatarioRepository destinatarioRepository;

    public DestinatarioService(DestinatarioRepository destinatarioRepository) {
        this.destinatarioRepository = destinatarioRepository;
    }

    public List<Destinatario> listar() {
        return destinatarioRepository.findAllByOrderByNombreAsc();
    }

    public Optional<Destinatario> porAzureSub(String azureSub) {
        return destinatarioRepository.findByAzureSub(azureSub);
    }

    /**
     * Alta o actualizacion del directorio (upsert por azureSub). Si el sub ya
     * existia se sobrescriben email, nombre y rol; activo solo si viene
     * informado para no dar de baja sin querer.
     */
    @Transactional(readOnly = false)
    public Destinatario alta(DestinatarioAltaRequest request) {
        Destinatario destinatario = destinatarioRepository
                .findByAzureSub(request.azureSub())
                .orElseGet(Destinatario::new);

        destinatario.setAzureSub(request.azureSub());
        destinatario.setEmail(request.email());
        destinatario.setNombre(request.nombre());
        destinatario.setRol(request.rol());
        if (request.activo() != null) {
            destinatario.setActivo(request.activo());
        }
        return destinatarioRepository.save(destinatario);
    }
}
