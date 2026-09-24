package com.califorge.msnotificaciones.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Notificacion: es a la vez cola de salida (outbox) e historial consultable.
 * La tabla notificacion se polla con @Scheduled para despachar las PENDIENTE
 * y REINTENTO cuyo proximo_intento_at ya vencio.
 */
@Entity
@Table(name = "notificacion")
public class Notificacion {

    public static final int MAX_INTENTOS_DEFECTO = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Size(max = 255, message = "destinatarioSub no puede superar 255 caracteres")
    @Column(name = "destinatario_sub", length = 255)
    private String destinatarioSub;

    @Size(max = 320, message = "destinatarioEmail no puede superar 320 caracteres")
    @Column(name = "destinatario_email", length = 320)
    private String destinatarioEmail;

    @Size(max = 255, message = "destinatarioNombre no puede superar 255 caracteres")
    @Column(name = "destinatario_nombre", length = 255)
    private String destinatarioNombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "canal", nullable = false, length = 20)
    private Canal canal = Canal.EMAIL;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoNotificacion tipo = TipoNotificacion.TRANSACCIONAL;

    @Column(name = "asunto", length = 500)
    private String asunto;

    @Column(name = "cuerpo_texto", columnDefinition = "text")
    private String cuerpoTexto;

    @Column(name = "cuerpo_html", columnDefinition = "text")
    private String cuerpoHtml;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoNotificacion estado = EstadoNotificacion.PENDIENTE;

    @Column(name = "idempotency_key", unique = true, length = 100)
    private String idempotencyKey;

    @Column(name = "intentos", nullable = false)
    private Integer intentos = 0;

    @Column(name = "max_intentos", nullable = false)
    private Integer maxIntentos = MAX_INTENTOS_DEFECTO;

    @Column(name = "proximo_intento_at")
    private LocalDateTime proximoIntentoAt;

    @Column(name = "ses_message_id", length = 100)
    private String sesMessageId;

    @Column(name = "payload_json", columnDefinition = "text")
    private String payloadJson;

    @Column(name = "origen_ms", length = 100)
    private String origenMs;

    @Column(name = "correlacion_id", length = 100)
    private String correlacionId;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = fechaCreacion;
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    public Notificacion() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getDestinatarioSub() { return destinatarioSub; }
    public void setDestinatarioSub(String destinatarioSub) { this.destinatarioSub = destinatarioSub; }

    public String getDestinatarioEmail() { return destinatarioEmail; }
    public void setDestinatarioEmail(String destinatarioEmail) { this.destinatarioEmail = destinatarioEmail; }

    public String getDestinatarioNombre() { return destinatarioNombre; }
    public void setDestinatarioNombre(String destinatarioNombre) { this.destinatarioNombre = destinatarioNombre; }

    public Canal getCanal() { return canal; }
    public void setCanal(Canal canal) { this.canal = canal; }

    public TipoNotificacion getTipo() { return tipo; }
    public void setTipo(TipoNotificacion tipo) { this.tipo = tipo; }

    public String getAsunto() { return asunto; }
    public void setAsunto(String asunto) { this.asunto = asunto; }

    public String getCuerpoTexto() { return cuerpoTexto; }
    public void setCuerpoTexto(String cuerpoTexto) { this.cuerpoTexto = cuerpoTexto; }

    public String getCuerpoHtml() { return cuerpoHtml; }
    public void setCuerpoHtml(String cuerpoHtml) { this.cuerpoHtml = cuerpoHtml; }

    public EstadoNotificacion getEstado() { return estado; }
    public void setEstado(EstadoNotificacion estado) { this.estado = estado; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public Integer getIntentos() { return intentos; }
    public void setIntentos(Integer intentos) { this.intentos = intentos; }

    public Integer getMaxIntentos() { return maxIntentos; }
    public void setMaxIntentos(Integer maxIntentos) { this.maxIntentos = maxIntentos; }

    public LocalDateTime getProximoIntentoAt() { return proximoIntentoAt; }
    public void setProximoIntentoAt(LocalDateTime proximoIntentoAt) { this.proximoIntentoAt = proximoIntentoAt; }

    public String getSesMessageId() { return sesMessageId; }
    public void setSesMessageId(String sesMessageId) { this.sesMessageId = sesMessageId; }

    public String getPayloadJson() { return payloadJson; }
    public void setPayloadJson(String payloadJson) { this.payloadJson = payloadJson; }

    public String getOrigenMs() { return origenMs; }
    public void setOrigenMs(String origenMs) { this.origenMs = origenMs; }

    public String getCorrelacionId() { return correlacionId; }
    public void setCorrelacionId(String correlacionId) { this.correlacionId = correlacionId; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
}
