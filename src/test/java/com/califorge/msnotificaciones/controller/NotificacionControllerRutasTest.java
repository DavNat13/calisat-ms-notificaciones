package com.califorge.msnotificaciones.controller;

import com.califorge.msnotificaciones.exception.GlobalExceptionHandler;
import com.califorge.msnotificaciones.service.NotificacionDetalleService;
import com.califorge.msnotificaciones.service.NotificacionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Regresion de enrutamiento: /mis-notificaciones no debe caer en /{id} y los
 * segmentos que no son UUID deben responder 404 (no 400 por conversion).
 */
class NotificacionControllerRutasTest {

    private static final String BASE = "/api/v1/notificaciones";
    private static final UUID ID = UUID.fromString("2f8f4d2e-1b3a-4c5d-9e8f-0a1b2c3d4e5f");

    private NotificacionService notificaciones;
    private NotificacionDetalleService detalle;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        notificaciones = mock(NotificacionService.class);
        detalle = mock(NotificacionDetalleService.class);
        mvc = MockMvcBuilders.standaloneSetup(new NotificacionController(notificaciones, detalle))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new JwtStub(), new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void misNotificacionesNoEsCapturadaPorElPathId() throws Exception {
        when(notificaciones.misNotificaciones(any(), any())).thenReturn(Page.empty());

        mvc.perform(get(BASE + "/mis-notificaciones")).andExpect(status().isOk());

        verify(notificaciones).misNotificaciones(eq("usuario-1"), any());
        verifyNoInteractions(detalle);
    }

    @Test
    void uuidValidoResuelveElDetalle() throws Exception {
        mvc.perform(get(BASE + "/" + ID)).andExpect(status().isOk());

        verify(detalle).detalleConIntentos(ID);
        verifyNoInteractions(notificaciones);
    }

    @Test
    void segmentoNoUuidResponde404SinInvocarElDetalle() throws Exception {
        mvc.perform(get(BASE + "/no-es-uuid")).andExpect(status().isNotFound());

        verifyNoInteractions(detalle, notificaciones);
    }

    /** GET sobre rutas que solo declaran POST responde 405 (nunca 500). */
    @Test
    void getSobreRutasSoloPostResponde405() throws Exception {
        mvc.perform(get(BASE + "/eventos")).andExpect(status().isMethodNotAllowed());
        mvc.perform(get(BASE + "/enviar")).andExpect(status().isMethodNotAllowed());
        mvc.perform(get(BASE + "/" + ID + "/reintentar")).andExpect(status().isMethodNotAllowed());

        verifyNoInteractions(detalle, notificaciones);
    }

    /** Ninguna ruta literal captura ni es capturada por /{id}. */
    @Test
    void rutasLiteralesDeGetSiguenDisjuntasDelId() throws Exception {
        when(notificaciones.misNotificaciones(any(), any())).thenReturn(Page.empty());
        when(notificaciones.historial(any(), any())).thenReturn(Page.empty());

        mvc.perform(get(BASE + "/mis-notificaciones")).andExpect(status().isOk());
        mvc.perform(get(BASE)).andExpect(status().isOk());

        verifyNoInteractions(detalle);
    }

    /** GET /api/v1/notificaciones?page=0&size=20 (paginacion del frontend)
     *  debe responder 200, nunca 405: la raiz solo declara GET. */
    @Test
    void paginacionPorConsultaDeGetNoResponde405() throws Exception {
        when(notificaciones.historial(any(), any())).thenReturn(Page.empty());

        mvc.perform(get(BASE + "?page=0&size=20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(notificaciones).historial(any(), any());
        verifyNoInteractions(detalle);
    }

    /** Sustituye al resolver de @AuthenticationPrincipal en el contexto standalone. */
    private static final class JwtStub implements HandlerMethodArgumentResolver {

        private static final Jwt JWT = new Jwt("token", Instant.now(), Instant.now().plusSeconds(300),
                Map.of("alg", "none"), Map.of("sub", "usuario-1"));

        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return Jwt.class.isAssignableFrom(parameter.getParameterType());
        }

        @Override
        public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer container,
                                      NativeWebRequest request, WebDataBinderFactory factory) {
            return JWT;
        }
    }
}
