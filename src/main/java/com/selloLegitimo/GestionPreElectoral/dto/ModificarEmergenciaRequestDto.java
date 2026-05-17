package com.selloLegitimo.GestionPreElectoral.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ModificarEmergenciaRequestDto {

    @NotBlank(message = "ciudadanoId es obligatorio")
    private String ciudadanoId;

    @NotBlank(message = "nuevaZona es obligatoria")
    private String nuevaZona;

    @NotBlank(message = "justificacion es obligatoria")
    @Size(min = 20, message = "justificacion debe tener al menos 20 caracteres")
    private String justificacion;

    @NotNull(message = "firmas multi-institucionales son obligatorias")
    @Valid
    private FirmasModificacionDto firmas;

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

    public FirmasModificacionDto getFirmas() {
        return firmas;
    }

    public void setFirmas(FirmasModificacionDto firmas) {
        this.firmas = firmas;
    }
}
