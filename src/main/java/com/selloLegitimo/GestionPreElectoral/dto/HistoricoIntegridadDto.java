package com.selloLegitimo.GestionPreElectoral.dto;

import java.time.LocalDateTime;

public class HistoricoIntegridadDto {

    private String hash;
    private LocalDateTime fecha;
    private String justificacion;
    private String firmanteSuperadmin;
    private String firmanteCne;

    public HistoricoIntegridadDto() {
    }

    public HistoricoIntegridadDto(
            String hash,
            LocalDateTime fecha,
            String justificacion,
            String firmanteSuperadmin,
            String firmanteCne
    ) {
        this.hash = hash;
        this.fecha = fecha;
        this.justificacion = justificacion;
        this.firmanteSuperadmin = firmanteSuperadmin;
        this.firmanteCne = firmanteCne;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getJustificacion() {
        return justificacion;
    }

    public void setJustificacion(String justificacion) {
        this.justificacion = justificacion;
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
}