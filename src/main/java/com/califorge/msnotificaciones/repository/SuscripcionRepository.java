package com.califorge.msnotificaciones.repository;

import com.califorge.msnotificaciones.model.Canal;
import com.califorge.msnotificaciones.model.Suscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SuscripcionRepository extends JpaRepository<Suscripcion, UUID> {

    List<Suscripcion> findByUsuarioSubOrderByTipoCampanaAsc(String usuarioSub);

    Optional<Suscripcion> findByUsuarioSubAndTipoCampanaAndCanal(String usuarioSub, String tipoCampana, Canal canal);
}
