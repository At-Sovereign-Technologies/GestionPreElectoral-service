package com.selloLegitimo.GestionPreElectoral.modelo;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

}
