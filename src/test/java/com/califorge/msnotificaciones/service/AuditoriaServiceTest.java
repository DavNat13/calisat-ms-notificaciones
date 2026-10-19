package com.califorge.msnotificaciones.service;

import com.califorge.msnotificaciones.dto.AuditoriaMensaje;
import com.califorge.msnotificaciones.model.AuditoriaEvento;
import com.califorge.msnotificaciones.model.ResultadoAuditoria;
import com.califorge.msnotificaciones.model.TipoAuditoria;
import com.califorge.msnotificaciones.repository.AuditoriaEventoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditoriaServiceTest {

    @Mock
    private AuditoriaEventoRepository repository;

    @InjectMocks
    private AuditoriaService service;

    private AuditoriaMensaje mensaje(ResultadoAuditoria resultado) {
        return AuditoriaMensaje.de(TipoAuditoria.ORDEN, "orden.confirmada", "orden-1",
                "sub-1", "ana@ejemplo.com", resultado, null);
    }

    @Test
    void registrarPersisteElEventoCompleto() {
        when(repository.findByClave(any())).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.registrar(mensaje(ResultadoAuditoria.ENVIADO));

        ArgumentCaptor<AuditoriaEvento> captor = ArgumentCaptor.forClass(AuditoriaEvento.class);
        verify(repository).save(captor.capture());

        AuditoriaEvento evento = captor.getValue();
        assertEquals(TipoAuditoria.ORDEN, evento.getTipo());
        assertEquals("orden.confirmada", evento.getEvento());
        assertEquals("orden-1", evento.getReferenciaId());
        assertEquals("sub-1", evento.getUsuarioSub());
        assertEquals("ana@ejemplo.com", evento.getDestinatarioEmail());
        assertEquals(ResultadoAuditoria.ENVIADO, evento.getResultadoCorreo());
    }

    @Test
    void claveRepetida_noDuplicaFilas() {
        when(repository.findByClave(any())).thenReturn(Optional.of(new AuditoriaEvento()));

        AuditoriaEvento resultado = service.registrar(mensaje(ResultadoAuditoria.ENVIADO));

        assertNull(resultado);
        verify(repository, never()).save(any());
    }

    @Test
    void mensajeNulo_lanzaError() {
        assertThrows(IllegalArgumentException.class, () -> service.registrar(null));
    }

    @Test
    void historialSinFiltrosUsaFindAll() {
        Page<AuditoriaEvento> esperada = new PageImpl<>(List.of());
        when(repository.findAllByOrderByFechaEventoDesc(any(Pageable.class))).thenReturn(esperada);

        Page<AuditoriaEvento> obtenida = service.historial(null, null, Pageable.unpaged());

        assertEquals(esperada, obtenida);
    }

    @Test
    void historialConTipoUsaElReposoPorTipo() {
        Page<AuditoriaEvento> esperada = new PageImpl<>(List.of());
        when(repository.findByTipoOrderByFechaEventoDesc(any(TipoAuditoria.class), any(Pageable.class)))
                .thenReturn(esperada);

        Page<AuditoriaEvento> obtenida = service.historial(TipoAuditoria.USUARIO, null, Pageable.unpaged());

        assertEquals(esperada, obtenida);
    }

    @Test
    void historialConResultadoUsaElReposoPorResultado() {
        Page<AuditoriaEvento> esperada = new PageImpl<>(List.of());
        when(repository.findByResultadoCorreoOrderByFechaEventoDesc(any(ResultadoAuditoria.class), any(Pageable.class)))
                .thenReturn(esperada);

        Page<AuditoriaEvento> obtenida = service.historial(null, ResultadoAuditoria.FALLIDO, Pageable.unpaged());

        assertEquals(esperada, obtenida);
    }

    @Test
    void historialConAmbosFiltrosUsaElReposoCompuesto() {
        Page<AuditoriaEvento> esperada = new PageImpl<>(List.of());
        when(repository.findByTipoAndResultadoCorreoOrderByFechaEventoDesc(
                any(TipoAuditoria.class), any(ResultadoAuditoria.class), any(Pageable.class)))
                .thenReturn(esperada);

        Page<AuditoriaEvento> obtenida =
                service.historial(TipoAuditoria.ENVIO, ResultadoAuditoria.ENVIADO, Pageable.unpaged());

        assertEquals(esperada, obtenida);
    }
}
