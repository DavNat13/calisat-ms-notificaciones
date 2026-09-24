package com.califorge.msnotificaciones.service;

import com.califorge.msnotificaciones.dto.PlantillaRequest;
import com.califorge.msnotificaciones.dto.PlantillaUpdateRequest;
import com.califorge.msnotificaciones.exception.PlantillaDuplicadaException;
import com.califorge.msnotificaciones.model.Plantilla;
import com.califorge.msnotificaciones.repository.PlantillaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlantillaServiceTest {

    @Mock
    private PlantillaRepository plantillaRepository;

    @InjectMocks
    private PlantillaService plantillaService;

    @Test
    void crear_persisteLaPlantillaNuevaConVersionUno() {
        when(plantillaRepository.existsByCodigo("BIENVENIDA")).thenReturn(false);
        when(plantillaRepository.save(any(Plantilla.class))).thenAnswer(invocation -> {
            Plantilla guardada = invocation.getArgument(0);
            guardada.setId(UUID.randomUUID());
            return guardada;
        });

        PlantillaRequest request = new PlantillaRequest(
                "BIENVENIDA", "Bienvenida, {{nombre}}", "Hola {{nombre}}", "<p>Hola</p>",
                "[\"nombre\"]", null);

        Plantilla plantilla = plantillaService.crear(request);

        ArgumentCaptor<Plantilla> captor = ArgumentCaptor.forClass(Plantilla.class);
        verify(plantillaRepository).save(captor.capture());
        assertEquals("BIENVENIDA", captor.getValue().getCodigo());
        assertEquals(1, plantilla.getVersion());
        assertEquals(Boolean.TRUE, plantilla.getActiva());
        assertEquals("Bienvenida, {{nombre}}", plantilla.getAsunto());
    }

    @Test
    void crear_lanzaDuplicadoSiElCodigoYaExiste() {
        when(plantillaRepository.existsByCodigo("BIENVENIDA")).thenReturn(true);

        PlantillaRequest request = new PlantillaRequest(
                "BIENVENIDA", "Asunto", "Texto", null, null, null);

        assertThrows(PlantillaDuplicadaException.class, () -> plantillaService.crear(request));
        verify(plantillaRepository, never()).save(any(Plantilla.class));
    }

    @Test
    void actualizar_incrementaLaVersionYLosCampos() {
        Plantilla existente = new Plantilla();
        existente.setId(UUID.randomUUID());
        existente.setCodigo("BIENVENIDA");
        existente.setAsunto("Asunto viejo");
        existente.setVersion(1);
        when(plantillaRepository.findByCodigo("BIENVENIDA")).thenReturn(Optional.of(existente));
        when(plantillaRepository.save(any(Plantilla.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PlantillaUpdateRequest request = new PlantillaUpdateRequest(
                "Asunto nuevo", "Cuerpo nuevo", null, "[\"nombre\"]", null);

        Plantilla actualizada = plantillaService.actualizar("BIENVENIDA", request);

        assertEquals(2, actualizada.getVersion());
        assertEquals("Asunto nuevo", actualizada.getAsunto());
        assertEquals("Cuerpo nuevo", actualizada.getCuerpoTexto());
        assertEquals(Boolean.TRUE, actualizada.getActiva());
    }

    @Test
    void eliminar_haceBajaLogicaSinBorrarElRegistro() {
        Plantilla existente = new Plantilla();
        existente.setId(UUID.randomUUID());
        existente.setCodigo("BIENVENIDA");
        existente.setActiva(Boolean.TRUE);
        existente.setVersion(1);
        when(plantillaRepository.findByCodigo("BIENVENIDA")).thenReturn(Optional.of(existente));
        when(plantillaRepository.save(any(Plantilla.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Plantilla eliminada = plantillaService.eliminar("BIENVENIDA");

        assertFalse(eliminada.getActiva());
        verify(plantillaRepository, never()).delete(any(Plantilla.class));
    }
}
