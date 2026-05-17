package com.selloLegitimo.GestionPreElectoral.dto;

public class LoginRespuestaDto {
    private String status;
    private String token;
    private Object user;
    private String message;

    public LoginRespuestaDto() {}

    public LoginRespuestaDto(String status, String token, Object user, String message) {
        this.status = status;
        this.token = token;
        this.user = user;
        this.message = message;
    }

    public static LoginRespuestaDto success(String token, Object user) {
        return new LoginRespuestaDto("AUTHENTICATED", token, user, "Login exitoso");
    }

    public static LoginRespuestaDto mfaSetupRequired(String tempToken, Object user) {
        return new LoginRespuestaDto("MFA_SETUP_REQUIRED", tempToken, user, "Debe configurar MFA antes de continuar");
    }

    public static LoginRespuestaDto mfaChallenge(String tempToken, Object user) {
        return new LoginRespuestaDto("MFA_CHALLENGE", tempToken, user, "Verifique su codigo MFA");
    }

    public static LoginRespuestaDto failed(String message) {
        return new LoginRespuestaDto("FAILED", null, null, message);
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

    public Object getUser() {
        return user;
    }

    public void setUser(Object user) {
        this.user = user;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}