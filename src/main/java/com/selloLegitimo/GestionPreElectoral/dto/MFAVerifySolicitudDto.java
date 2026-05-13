package com.selloLegitimo.GestionPreElectoral.dto;

import jakarta.validation.constraints.NotBlank;

public class MFAVerifySolicitudDto {
    @NotBlank
    private String otpCode;

    public MFAVerifySolicitudDto() {}

    public String getOtpCode() {
        return otpCode;
    }

    public void setOtpCode(String otpCode) {
        this.otpCode = otpCode;
    }
}