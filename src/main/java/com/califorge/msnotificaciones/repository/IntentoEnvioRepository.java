package com.califorge.msnotificaciones.repository;

import com.califorge.msnotificaciones.model.IntentoEnvio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IntentoEnvioRepository extends JpaRepository<IntentoEnvio, UUID> {

    List<IntentoEnvio> findByNotificacionIdOrderByNumeroIntentoAsc(UUID notificacionId);
}
