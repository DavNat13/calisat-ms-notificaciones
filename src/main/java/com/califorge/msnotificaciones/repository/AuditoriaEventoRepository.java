package com.califorge.msnotificaciones.repository;

import com.califorge.msnotificaciones.model.AuditoriaEvento;
import com.califorge.msnotificaciones.model.ResultadoAuditoria;
import com.califorge.msnotificaciones.model.TipoAuditoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuditoriaEventoRepository extends JpaRepository<AuditoriaEvento, UUID> {

    Optional<AuditoriaEvento> findByClave(String clave);

    Page<AuditoriaEvento> findAllByOrderByFechaEventoDesc(Pageable pageable);

    Page<AuditoriaEvento> findByTipoOrderByFechaEventoDesc(TipoAuditoria tipo, Pageable pageable);

    Page<AuditoriaEvento> findByResultadoCorreoOrderByFechaEventoDesc(ResultadoAuditoria resultadoCorreo, Pageable pageable);

    Page<AuditoriaEvento> findByTipoAndResultadoCorreoOrderByFechaEventoDesc(
            TipoAuditoria tipo, ResultadoAuditoria resultadoCorreo, Pageable pageable);
}
