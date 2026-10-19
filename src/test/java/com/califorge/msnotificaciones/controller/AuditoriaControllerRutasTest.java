package com.califorge.msnotificaciones.controller;

import com.califorge.msnotificaciones.model.AuditoriaEvento;
import com.califorge.msnotificaciones.model.ResultadoAuditoria;
import com.califorge.msnotificaciones.model.TipoAuditoria;
import com.califorge.msnotificaciones.service.AuditoriaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Regresion de enrutamiento y serializacion del listado de auditoria
 * (solo ROLE_ADMINISTRADOR; los filtros son opcionales).
 */
class AuditoriaControllerRutasTest {

    private static final String BASE = "/api/v1/auditoria";

    private AuditoriaService auditoria;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        auditoria = mock(AuditoriaService.class);
        mvc = MockMvcBuilders.standaloneSetup(new AuditoriaController(auditoria))
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    private AuditoriaEvento evento() {
        AuditoriaEvento evento = new AuditoriaEvento();
        evento.setId(UUID.fromString("2f8f4d2e-1b3a-4c5d-9e8f-0a1b2c3d4e5f"));
        evento.setClave("ORDEN:orden.confirmada:orden-1:2026-01-01T00:00:00Z");
        evento.setTipo(TipoAuditoria.ORDEN);
        evento.setEvento("orden.confirmada");
        evento.setReferenciaId("orden-1");
        evento.setUsuarioSub("sub-1");
        evento.setDestinatarioEmail("ana@ejemplo.com");
        evento.setResultadoCorreo(ResultadoAuditoria.ENVIADO);
        evento.setFechaEvento(Instant.parse("2026-01-01T00:00:00Z"));
        return evento;
    }

    @Test
    void listadoSinFiltrosResponde200() throws Exception {
        when(auditoria.historial(isNull(), isNull(), any()))
                .thenReturn(new PageImpl<>(List.of(evento())));

        mvc.perform(get(BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].tipo").value("ORDEN"))
                .andExpect(jsonPath("$.content[0].resultadoCorreo").value("ENVIADO"));

        verify(auditoria).historial(isNull(), isNull(), any());
    }

    @Test
    void filtrosTipoYResultadoSePasanAlServicio() throws Exception {
        when(auditoria.historial(eq(TipoAuditoria.USUARIO), eq(ResultadoAuditoria.FALLIDO), any()))
                .thenReturn(Page.empty());

        mvc.perform(get(BASE + "?tipo=USUARIO&resultado=FALLIDO"))
                .andExpect(status().isOk());

        verify(auditoria).historial(eq(TipoAuditoria.USUARIO), eq(ResultadoAuditoria.FALLIDO), any());
    }

    @Test
    void filtroDeValorInvalidoResponde400() throws Exception {
        mvc.perform(get(BASE + "?tipo=NO_EXISTE")).andExpect(status().isBadRequest());
    }

    @Test
    void paginacionDeConsultaResponde200() throws Exception {
        when(auditoria.historial(isNull(), isNull(), any())).thenReturn(Page.empty());

        mvc.perform(get(BASE + "?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }
}
