package com.selloLegitimo.GestionPreElectoral.dto;

public class MFAVerifyRespuestaDto {
    private String status;
    private String token;
    private String message;

    public MFAVerifyRespuestaDto() {}

    public MFAVerifyRespuestaDto(String status, String token, String message) {
        this.status = status;
        this.token = token;
        this.message = message;
    }

    public static MFAVerifyRespuestaDto success(String token) {
        return new MFAVerifyRespuestaDto("MFA_VERIFIED", token, "Autenticacion MFA exitosa");
    }

    public static MFAVerifyRespuestaDto failed(String message) {
        return new MFAVerifyRespuestaDto("MFA_FAILED", null, message);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}