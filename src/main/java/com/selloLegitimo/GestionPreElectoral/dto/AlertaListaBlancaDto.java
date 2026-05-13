package com.selloLegitimo.GestionPreElectoral.dto;

public class AlertaListaBlancaDto {

    private String ciudadanoId;
    private String motivo;

    public AlertaListaBlancaDto() {
    }

    public AlertaListaBlancaDto(String ciudadanoId, String motivo) {
        this.ciudadanoId = ciudadanoId;
        this.motivo = motivo;
    }

    public String getCiudadanoId() {
        return ciudadanoId;
    }

    public void setCiudadanoId(String ciudadanoId) {
        this.ciudadanoId = ciudadanoId;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
