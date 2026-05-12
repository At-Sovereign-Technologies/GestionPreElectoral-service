package com.selloLegitimo.GestionPreElectoral.modelo;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "lista_blanca_auditoria", schema = "gestion_pre_electoral")
public class ListaBlancaAuditoria {

    @Id
    private UUID id;

    @Column(name = "lista_blanca_id")
    private UUID listaBlancaId;

    @Column(name = "justificacion")
    private String justificacion;

    @Column(name = "firmas_json", columnDefinition = "jsonb")
    private String firmasJson;

    @Column(name = "version_hash")
    private String versionHash;

    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    @Column(name = "zona_anterior")
    private String zonaAnterior;

    @Column(name = "zona_nueva")
    private String zonaNueva;

    @Column(name = "firmante_superadmin")
    private String firmanteSuperadmin;

    @Column(name = "firmante_cne")
    private String firmanteCne;

    @Column(name = "evento_auditoria_id")
    private Long eventoAuditoriaId;

    public ListaBlancaAuditoria() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getListaBlancaId() {
        return listaBlancaId;
    }

    public void setListaBlancaId(UUID listaBlancaId) {
        this.listaBlancaId = listaBlancaId;
    }

    public String getJustificacion() {
        return justificacion;
    }

    public void setJustificacion(String justificacion) {
        this.justificacion = justificacion;
    }

    public String getFirmasJson() {
        return firmasJson;
    }

    public void setFirmasJson(String firmasJson) {
        this.firmasJson = firmasJson;
    }

    public String getVersionHash() {
        return versionHash;
    }

    public void setVersionHash(String versionHash) {
        this.versionHash = versionHash;
    }

    public LocalDateTime getFechaModificacion() {
        return fechaModificacion;
    }

    public void setFechaModificacion(LocalDateTime fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }

    public String getZonaAnterior() {
        return zonaAnterior;
    }

    public void setZonaAnterior(String zonaAnterior) {
        this.zonaAnterior = zonaAnterior;
    }

    public String getZonaNueva() {
        return zonaNueva;
    }

    public void setZonaNueva(String zonaNueva) {
        this.zonaNueva = zonaNueva;
    }

    public String getFirmanteSuperadmin() {
        return firmanteSuperadmin;
    }

    public void setFirmanteSuperadmin(String firmanteSuperadmin) {
        this.firmanteSuperadmin = firmanteSuperadmin;
    }

    public String getFirmanteCne() {
        return firmanteCne;
    }

    public void setFirmanteCne(String firmanteCne) {
        this.firmanteCne = firmanteCne;
    }

    public Long getEventoAuditoriaId() {
        return eventoAuditoriaId;
    }

    public void setEventoAuditoriaId(Long eventoAuditoriaId) {
        this.eventoAuditoriaId = eventoAuditoriaId;
    }
}
