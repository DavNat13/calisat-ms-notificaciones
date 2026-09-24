package com.califorge.msnotificaciones.repository;

import com.califorge.msnotificaciones.model.EstadoNotificacion;
import com.califorge.msnotificaciones.model.Notificacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, UUID> {

    Optional<Notificacion> findByIdempotencyKey(String idempotencyKey);

    Page<Notificacion> findByDestinatarioSub(String destinatarioSub, Pageable pageable);

    Page<Notificacion> findByEstado(EstadoNotificacion estado, Pageable pageable);

    Page<Notificacion> findAllByOrderByFechaCreacionDesc(Pageable pageable);

    Page<Notificacion> findByEstadoOrderByFechaCreacionDesc(EstadoNotificacion estado, Pageable pageable);

    List<Notificacion> findByEstadoIn(Collection<EstadoNotificacion> estados);
}
