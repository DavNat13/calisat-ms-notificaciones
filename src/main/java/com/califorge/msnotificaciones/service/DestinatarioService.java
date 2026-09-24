package com.califorge.msnotificaciones.service;

import com.califorge.msnotificaciones.model.Destinatario;
import com.califorge.msnotificaciones.repository.DestinatarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Directorio de destinatarios (solo lectura en la fase A).
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
}
