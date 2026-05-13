package com.selloLegitimo.GestionPreElectoral.dto;

public class VerificarIntegridadResponseDto {

    private String hashIntegridad;
    private String estado;

    public VerificarIntegridadResponseDto() {}

    public VerificarIntegridadResponseDto(String hashIntegridad, String estado) {
        this.hashIntegridad = hashIntegridad;
        this.estado = estado;
    }

    public String getHashIntegridad() {
        return hashIntegridad;
    }

    public void setHashIntegridad(String hashIntegridad) {
        this.hashIntegridad = hashIntegridad;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
