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
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Preferencia de suscripcion a campanas por usuario y canal.
 * opt_in es FALSE por defecto (consentimiento previo, Ley 1581/2012);
 * el token_desuscripcion permite la baja en 1 clic desde el email.
 */
@Entity
@Table(name = "suscripcion", uniqueConstraints = {
        @UniqueConstraint(name = "uk_suscripcion_usuario_campana",
                columnNames = {"usuario_sub", "tipo_campana", "canal"})
})
public class Suscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "usuarioSub es obligatorio")
    @Size(max = 255, message = "usuarioSub no puede superar 255 caracteres")
    @Column(name = "usuario_sub", nullable = false, length = 255)
    private String usuarioSub;

    @NotBlank(message = "tipoCampana es obligatorio")
    @Size(max = 100, message = "tipoCampana no puede superar 100 caracteres")
    @Column(name = "tipo_campana", nullable = false, length = 100)
    private String tipoCampana;

    @Enumerated(EnumType.STRING)
    @Column(name = "canal", nullable = false, length = 20)
    private Canal canal = Canal.EMAIL;

    @Column(name = "opt_in", nullable = false)
    private Boolean optIn = false;

    @Size(max = 64, message = "tokenDesuscripcion no puede superar 64 caracteres")
    @Column(name = "token_desuscripcion", unique = true, length = 64)
    private String tokenDesuscripcion;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = fechaCreacion;
        if (tokenDesuscripcion == null || tokenDesuscripcion.isBlank()) {
            tokenDesuscripcion = UUID.randomUUID().toString();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    public Suscripcion() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getUsuarioSub() { return usuarioSub; }
    public void setUsuarioSub(String usuarioSub) { this.usuarioSub = usuarioSub; }

    public String getTipoCampana() { return tipoCampana; }
    public void setTipoCampana(String tipoCampana) { this.tipoCampana = tipoCampana; }

    public Canal getCanal() { return canal; }
    public void setCanal(Canal canal) { this.canal = canal; }

    public Boolean getOptIn() { return optIn; }
    public void setOptIn(Boolean optIn) { this.optIn = optIn; }

    public String getTokenDesuscripcion() { return tokenDesuscripcion; }
    public void setTokenDesuscripcion(String tokenDesuscripcion) { this.tokenDesuscripcion = tokenDesuscripcion; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
}
