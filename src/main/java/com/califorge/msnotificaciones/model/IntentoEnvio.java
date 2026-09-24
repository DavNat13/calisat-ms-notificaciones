package com.califorge.msnotificaciones.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Traza de cada intento de envio de una notificacion
 * (proveedor, messageId, http_status y error para auditoria).
 */
@Entity
@Table(name = "intento_envio")
public class IntentoEnvio {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "notificacion_id", nullable = false)
    private Notificacion notificacion;

    @Column(name = "numero_intento", nullable = false)
    private Integer numeroIntento;

    @Column(name = "proveedor", length = 50)
    private String proveedor;

    @Column(name = "message_id", length = 100)
    private String messageId;

    @Column(name = "http_status")
    private Integer httpStatus;

    @Column(name = "error", columnDefinition = "text")
    private String error;

    @Column(name = "exito", nullable = false)
    private Boolean exito = false;

    @Column(name = "fecha_intento", nullable = false)
    private LocalDateTime fechaIntento;

    @PrePersist
    protected void onCreate() {
        fechaIntento = LocalDateTime.now();
    }

    public IntentoEnvio() {}

    public IntentoEnvio(Notificacion notificacion, Integer numeroIntento, String proveedor,
                        String messageId, Integer httpStatus, String error, Boolean exito) {
        this.notificacion = notificacion;
        this.numeroIntento = numeroIntento;
        this.proveedor = proveedor;
        this.messageId = messageId;
        this.httpStatus = httpStatus;
        this.error = error;
        this.exito = exito;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Notificacion getNotificacion() { return notificacion; }
    public void setNotificacion(Notificacion notificacion) { this.notificacion = notificacion; }

    public Integer getNumeroIntento() { return numeroIntento; }
    public void setNumeroIntento(Integer numeroIntento) { this.numeroIntento = numeroIntento; }

    public String getProveedor() { return proveedor; }
    public void setProveedor(String proveedor) { this.proveedor = proveedor; }

    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public Integer getHttpStatus() { return httpStatus; }
    public void setHttpStatus(Integer httpStatus) { this.httpStatus = httpStatus; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public Boolean getExito() { return exito; }
    public void setExito(Boolean exito) { this.exito = exito; }

    public LocalDateTime getFechaIntento() { return fechaIntento; }
    public void setFechaIntento(LocalDateTime fechaIntento) { this.fechaIntento = fechaIntento; }
}
