package com.califorge.msnotificaciones.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NotificacionNoEncontradaException.class)
    public ResponseEntity<Map<String, Object>> notificacionNoEncontrada(NotificacionNoEncontradaException ex) {
        log.warn("Notificacion no encontrada: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("mensaje", ex.getMessage()));
    }

    @ExceptionHandler(PlantillaNoEncontradaException.class)
    public ResponseEntity<Map<String, Object>> plantillaNoEncontrada(PlantillaNoEncontradaException ex) {
        log.warn("Plantilla no encontrada: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("mensaje", ex.getMessage()));
    }

    @ExceptionHandler(PlantillaDuplicadaException.class)
    public ResponseEntity<Map<String, Object>> plantillaDuplicada(PlantillaDuplicadaException ex) {
        log.warn("Plantilla duplicada rechazada: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("mensaje", ex.getMessage()));
    }

    @ExceptionHandler(TransicionEstadoNoPermitidaException.class)
    public ResponseEntity<Map<String, Object>> transicionNoPermitida(TransicionEstadoNoPermitidaException ex) {
        log.warn("Transicion de estado no permitida: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("mensaje", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validacion(MethodArgumentNotValidException ex) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Datos invalidos");
        log.warn("Validacion de entrada rechazada: {}", mensaje);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("mensaje", mensaje));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> requestMalformado(HttpMessageNotReadableException ex) {
        log.warn("Cuerpo de solicitud no legible: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("mensaje", "El cuerpo de la solicitud no es un JSON valido"));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> tipoArgumentoInvalido(MethodArgumentTypeMismatchException ex) {
        log.warn("Tipo de argumento invalido para '{}': {}", ex.getName(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("mensaje", "Parametro invalido: " + ex.getName()));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> parametroFaltante(MissingServletRequestParameterException ex) {
        log.warn("Parametro requerido faltante: {}", ex.getParameterName());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("mensaje", "Falta el parametro requerido: " + ex.getParameterName()));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> noResourceFound(NoResourceFoundException ex) {
        log.warn("Ruta no encontrada: {} {}", ex.getHttpMethod(), ex.getResourcePath());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("mensaje", "Recurso no encontrado"));
    }

    /** Spring Framework 7 lanza NoHandlerFoundException cuando ningun mapping
     *  (ni los literales ni /{id:UUID}) captura la ruta; se responde 404 y no 500. */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Map<String, Object>> noHandlerFound(NoHandlerFoundException ex) {
        log.warn("Endpoint no encontrado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("mensaje", "Recurso no encontrado"));
    }

    /** GET sobre una ruta que solo declara POST/PUT/DELETE (p. ej.
     *  /eventos, /enviar, /{id}/reintentar) responde 405 y no 500. */
    @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> metodoNoSoportado(
            org.springframework.web.HttpRequestMethodNotSupportedException ex) {
        log.warn("Metodo HTTP no soportado para {}: soportados {}", ex.getRequestURL(), ex.getSupportedMethods());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(Map.of("mensaje", "Metodo HTTP no soportado para este recurso",
                        "soportados", String.join(", ", ex.getSupportedMethods())));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> integridad(DataIntegrityViolationException ex) {
        log.warn("Restriccion de datos violada: {}", ex.getMostSpecificCause().getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("mensaje", "El registro viola una restriccion de datos (posible clave unica duplicada)"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> generico(Exception ex) {
        log.error("Error no controlado en notificaciones", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("mensaje", "Error interno del servidor"));
    }
}
