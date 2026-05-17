package com.selloLegitimo.GestionPreElectoral.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginSolicitudDto {
    @NotBlank
    private String numeroDocumento;

    @NotBlank
    private String contrasena;

    private String ipAddress;
    private String deviceId;

    public LoginSolicitudDto() {}

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}