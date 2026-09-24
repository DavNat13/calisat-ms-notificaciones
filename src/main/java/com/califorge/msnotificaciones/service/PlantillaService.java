package com.califorge.msnotificaciones.service;

import com.califorge.msnotificaciones.dto.PlantillaRequest;
import com.califorge.msnotificaciones.dto.PlantillaUpdateRequest;
import com.califorge.msnotificaciones.exception.PlantillaDuplicadaException;
import com.califorge.msnotificaciones.exception.PlantillaNoEncontradaException;
import com.califorge.msnotificaciones.model.Plantilla;
import com.califorge.msnotificaciones.repository.PlantillaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CRUD de plantillas. DELETE es baja logica: marca activa=false
 * y conserva el registro para auditoria e historial.
 */
@Service
@Transactional
public class PlantillaService {

    private final PlantillaRepository plantillaRepository;

    public PlantillaService(PlantillaRepository plantillaRepository) {
        this.plantillaRepository = plantillaRepository;
    }

    @Transactional(readOnly = true)
    public List<Plantilla> listar() {
        return plantillaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Plantilla obtener(String codigo) {
        return plantillaRepository.findByCodigo(codigo)
                .orElseThrow(() -> new PlantillaNoEncontradaException(codigo));
    }

    public Plantilla crear(PlantillaRequest request) {
        if (plantillaRepository.existsByCodigo(request.codigo())) {
            throw new PlantillaDuplicadaException(request.codigo());
        }
        Plantilla plantilla = new Plantilla();
        plantilla.setCodigo(request.codigo());
        plantilla.setAsunto(request.asunto());
        plantilla.setCuerpoTexto(request.cuerpoTexto());
        plantilla.setCuerpoHtml(request.cuerpoHtml());
        plantilla.setVariables(request.variables());
        plantilla.setActiva(request.activa() != null ? request.activa() : Boolean.TRUE);
        plantilla.setVersion(1);
        return plantillaRepository.save(plantilla);
    }

    public Plantilla actualizar(String codigo, PlantillaUpdateRequest request) {
        Plantilla plantilla = obtener(codigo);
        plantilla.setAsunto(request.asunto());
        plantilla.setCuerpoTexto(request.cuerpoTexto());
        plantilla.setCuerpoHtml(request.cuerpoHtml());
        plantilla.setVariables(request.variables());
        if (request.activa() != null) {
            plantilla.setActiva(request.activa());
        }
        plantilla.setVersion(plantilla.getVersion() + 1);
        return plantillaRepository.save(plantilla);
    }

    public Plantilla eliminar(String codigo) {
        Plantilla plantilla = obtener(codigo);
        plantilla.setActiva(Boolean.FALSE);
        return plantillaRepository.save(plantilla);
    }
}
