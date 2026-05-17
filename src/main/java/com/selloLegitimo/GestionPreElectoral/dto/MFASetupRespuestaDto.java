package com.selloLegitimo.GestionPreElectoral.dto;

public class MFASetupRespuestaDto {
    private String qrCodeUrl;
    private String secret;
    private String message;

    public MFASetupRespuestaDto() {}

    public MFASetupRespuestaDto(String qrCodeUrl, String secret, String message) {
        this.qrCodeUrl = qrCodeUrl;
        this.secret = secret;
        this.message = message;
    }

    public static MFASetupRespuestaDto mocked() {
        return new MFASetupRespuestaDto(
            "mock://totp/setup",
            "MOCK_TOTP_SECRET",
            "TOTP setup mocked - integrate real TOTP library (e.g. speakeasy, otplib) to generate real secret + QR URI"
        );
    }

    public String getQrCodeUrl() {
        return qrCodeUrl;
    }

    public void setQrCodeUrl(String qrCodeUrl) {
        this.qrCodeUrl = qrCodeUrl;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}