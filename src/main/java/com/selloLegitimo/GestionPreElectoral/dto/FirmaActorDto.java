package com.selloLegitimo.GestionPreElectoral.dto;

import jakarta.validation.constraints.NotBlank;

public class FirmaActorDto {

    @NotBlank(message = "el usuario del firmante es obligatorio")
    private String usuario;

    @NotBlank(message = "la firma del actor es obligatoria")
    private String firma;

    public FirmaActorDto() {
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getFirma() {
        return firma;
    }

    public void setFirma(String firma) {
        this.firma = firma;
    }
}
