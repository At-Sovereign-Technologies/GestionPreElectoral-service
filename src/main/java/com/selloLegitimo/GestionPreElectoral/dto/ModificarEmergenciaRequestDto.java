package com.selloLegitimo.GestionPreElectoral.dto;

import com.fasterxml.jackson.databind.JsonNode;

public class ModificarEmergenciaRequestDto {

    private String ciudadanoId;
    private String nuevaZona;
    private String justificacion;
    private JsonNode firmas; // JSON con firmas múltiples (superadmin, CNE, etc.)

    public String getCiudadanoId() {
        return ciudadanoId;
    }

    public void setCiudadanoId(String ciudadanoId) {
        this.ciudadanoId = ciudadanoId;
    }

    public String getNuevaZona() {
        return nuevaZona;
    }

    public void setNuevaZona(String nuevaZona) {
        this.nuevaZona = nuevaZona;
    }

    public String getJustificacion() {
        return justificacion;
    }

    public void setJustificacion(String justificacion) {
        this.justificacion = justificacion;
    }

    public JsonNode getFirmas() {
        return firmas;
    }

    public void setFirmas(JsonNode firmas) {
        this.firmas = firmas;
    }
}
