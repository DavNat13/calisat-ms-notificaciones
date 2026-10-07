package com.califorge.msnotificaciones.service;

import com.califorge.msnotificaciones.dto.DestinatarioAltaRequest;
import com.califorge.msnotificaciones.model.Destinatario;
import com.califorge.msnotificaciones.repository.DestinatarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DestinatarioServiceTest {

    private static final String SUB = "00000000-0000-0000-0000-000000000001";

    @Mock
    private DestinatarioRepository destinatarioRepository;

    @InjectMocks
    private DestinatarioService destinatarioService;

    @Test
    void altaNueva_creaElRegistroActivo() {
        when(destinatarioRepository.findByAzureSub(SUB)).thenReturn(Optional.empty());
        when(destinatarioRepository.save(any(Destinatario.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Destinatario guardado = destinatarioService.alta(
                new DestinatarioAltaRequest(SUB, "ana@ejemplo.com", "Ana Perez", "cliente", null));

        assertEquals(SUB, guardado.getAzureSub());
        assertEquals("ana@ejemplo.com", guardado.getEmail());
        assertEquals("Ana Perez", guardado.getNombre());
        assertEquals("cliente", guardado.getRol());
        assertTrue(guardado.getActivo());
    }

    @Test
    void altaExistente_actualizaSinDarDeBaja() {
        Destinatario existente = new Destinatario();
        existente.setAzureSub(SUB);
        existente.setEmail("viejo@ejemplo.com");
        existente.setNombre("Viejo");
        existente.setActivo(true);
        when(destinatarioRepository.findByAzureSub(SUB)).thenReturn(Optional.of(existente));
        when(destinatarioRepository.save(any(Destinatario.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Destinatario guardado = destinatarioService.alta(
                new DestinatarioAltaRequest(SUB, "nuevo@ejemplo.com", "Nuevo Nombre", "cliente", null));

        assertEquals("nuevo@ejemplo.com", guardado.getEmail());
        assertEquals("Nuevo Nombre", guardado.getNombre());
        assertTrue(guardado.getActivo());
    }

    @Test
    void altaConActivoFalse_daDeBajaElDestinatario() {
        Destinatario existente = new Destinatario();
        existente.setAzureSub(SUB);
        existente.setActivo(true);
        when(destinatarioRepository.findByAzureSub(SUB)).thenReturn(Optional.of(existente));
        when(destinatarioRepository.save(any(Destinatario.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Destinatario guardado = destinatarioService.alta(
                new DestinatarioAltaRequest(SUB, "ana@ejemplo.com", "Ana", "cliente", false));

        assertFalse(guardado.getActivo());
        verify(destinatarioRepository).save(existente);
    }

    @Test
    void altaDosVeces_noDuplicaElSub() {
        Destinatario primera = new Destinatario();
        primera.setAzureSub(SUB);
        primera.setEmail("a@e.com");
        when(destinatarioRepository.findByAzureSub(SUB))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(primera));
        when(destinatarioRepository.save(any(Destinatario.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        destinatarioService.alta(new DestinatarioAltaRequest(SUB, "a@e.com", "A", null, null));
        Destinatario segunda = destinatarioService.alta(
                new DestinatarioAltaRequest(SUB, "b@e.com", "B", null, null));

        assertEquals("b@e.com", segunda.getEmail());
        assertEquals(primera.getId(), segunda.getId());
        verify(destinatarioRepository, times(2)).save(any(Destinatario.class));
    }

    @Test
    void listar_delegaOrdenadoPorNombre() {
        Destinatario uno = new Destinatario();
        uno.setId(UUID.randomUUID());
        when(destinatarioRepository.findAllByOrderByNombreAsc()).thenReturn(List.of(uno));

        assertEquals(1, destinatarioService.listar().size());
        verify(destinatarioRepository).findAllByOrderByNombreAsc();
    }

    @Test
    void porAzureSub_devuelveVacioSiNoExiste() {
        when(destinatarioRepository.findByAzureSub(SUB)).thenReturn(Optional.empty());

        assertTrue(destinatarioService.porAzureSub(SUB).isEmpty());
        verify(destinatarioRepository, never()).findAllByOrderByNombreAsc();
    }
}
