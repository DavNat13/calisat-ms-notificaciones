package com.califorge.msnotificaciones.repository;

import com.califorge.msnotificaciones.model.Plantilla;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlantillaRepository extends JpaRepository<Plantilla, UUID> {

    Optional<Plantilla> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);
}
