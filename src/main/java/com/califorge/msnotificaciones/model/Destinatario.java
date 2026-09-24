package com.califorge.msnotificaciones.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Directorio de destinatarios conocidos (extraido de los perfiles de usuario).
 * El flag activo permite dar de baja sin borrar el registro (bounces, etc.).
 */
@Entity
@Table(name = "destinatario")
public class Destinatario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "azureSub es obligatorio")
    @Size(max = 255, message = "azureSub no puede superar 255 caracteres")
    @Column(name = "azure_sub", nullable = false, unique = true, length = 255)
    private String azureSub;

    @Size(max = 320, message = "email no puede superar 320 caracteres")
    @Column(name = "email", length = 320)
    private String email;

    @Size(max = 255, message = "nombre no puede superar 255 caracteres")
    @Column(name = "nombre", length = 255)
    private String nombre;

    @Size(max = 100, message = "rol no puede superar 100 caracteres")
    @Column(name = "rol", length = 100)
    private String rol;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

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

    public Destinatario() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getAzureSub() { return azureSub; }
    public void setAzureSub(String azureSub) { this.azureSub = azureSub; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
}
