package com.selloLegitimo.GestionPreElectoral.modelo;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "lista_blanca", schema = "gestion_pre_electoral")
public class ListaBlanca {

    @Id
    private UUID id;

    @Column(name = "ciudadano_id")
    private String ciudadanoId;

    @Column(name = "eleccion_id")
    private Long eleccionId;

    @Column(name = "numero_documento")
    private String numeroDocumento;

    @Column(name = "telefono_celular")
    private String telefonoCelular;

    @Column(name = "correo_electronico")
    private String correoElectronico;

    @Column(name = "hash_biometrico_facial")
    private String hashBiometricoFacial;

    @Column(name = "zona_inscripcion")
    private String zonaInscripcion;

    @Column(name = "fecha_enrolamiento")
    private LocalDateTime fechaEnrolamiento;

    @Column(name = "estado")
    private String estado;

    @Column(name = "mfa_enabled", nullable = false)
    private boolean mfaEnabled = false;

    @Column(name = "mfa_secret", length = 512)
    private String mfaSecret;

    @Column(name = "mfa_configured_at")
    private LocalDateTime mfaConfiguredAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "mfa_method", length = 20, nullable = false)
    private MetodoMFA mfaMethod = MetodoMFA.NONE;

    @Column(name = "failed_attempts", nullable = false)
    private int failedAttempts = 0;

    @Column(name = "last_failed_at")
    private LocalDateTime lastFailedAt;

    @Column(name = "contrasena_hash", length = 256)
    private String contrasenaHash;

    @Column(name = "rol")
    private String rol;

    public ListaBlanca() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCiudadanoId() {
        return ciudadanoId;
    }

    public void setCiudadanoId(String ciudadanoId) {
        this.ciudadanoId = ciudadanoId;
    }

    public Long getEleccionId() {
        return eleccionId;
    }

    public void setEleccionId(Long eleccionId) {
        this.eleccionId = eleccionId;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public String getTelefonoCelular() {
        return telefonoCelular;
    }

    public void setTelefonoCelular(String telefonoCelular) {
        this.telefonoCelular = telefonoCelular;
    }

    public String getCorreoElectronico() {
        return correoElectronico;
    }

    public void setCorreoElectronico(String correoElectronico) {
        this.correoElectronico = correoElectronico;
    }

    public String getHashBiometricoFacial() {
        return hashBiometricoFacial;
    }

    public void setHashBiometricoFacial(String hashBiometricoFacial) {
        this.hashBiometricoFacial = hashBiometricoFacial;
    }

    public String getZonaInscripcion() {
        return zonaInscripcion;
    }

    public void setZonaInscripcion(String zonaInscripcion) {
        this.zonaInscripcion = zonaInscripcion;
    }

    public LocalDateTime getFechaEnrolamiento() {
        return fechaEnrolamiento;
    }

    public void setFechaEnrolamiento(LocalDateTime fechaEnrolamiento) {
        this.fechaEnrolamiento = fechaEnrolamiento;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public boolean isMfaEnabled() {
        return mfaEnabled;
    }

    public void setMfaEnabled(boolean mfaEnabled) {
        this.mfaEnabled = mfaEnabled;
    }

    public String getMfaSecret() {
        return mfaSecret;
    }

    public void setMfaSecret(String mfaSecret) {
        this.mfaSecret = mfaSecret;
    }

    public LocalDateTime getMfaConfiguredAt() {
        return mfaConfiguredAt;
    }

    public void setMfaConfiguredAt(LocalDateTime mfaConfiguredAt) {
        this.mfaConfiguredAt = mfaConfiguredAt;
    }

    public MetodoMFA getMfaMethod() {
        return mfaMethod;
    }

    public void setMfaMethod(MetodoMFA mfaMethod) {
        this.mfaMethod = mfaMethod;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public void setFailedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    public LocalDateTime getLastFailedAt() {
        return lastFailedAt;
    }

    public void setLastFailedAt(LocalDateTime lastFailedAt) {
        this.lastFailedAt = lastFailedAt;
    }

    public String getContrasenaHash() {
        return contrasenaHash;
    }

    public void setContrasenaHash(String contrasenaHash) {
        this.contrasenaHash = contrasenaHash;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }
}