package com.selloLegitimo.GestionPreElectoral.dto;

import java.util.Map;

public class UsuarioAutenticadoDto {
    private String numeroDocumento;
    private String nombre;
    private String rol;
    private String telefono;
    private String correo;
    private boolean mfaEnabled;

    public UsuarioAutenticadoDto() {}

    public UsuarioAutenticadoDto(String numeroDocumento, String nombre, String rol,
                                  String telefono, String correo, boolean mfaEnabled) {
        this.numeroDocumento = numeroDocumento;
        this.nombre = nombre;
        this.rol = rol;
        this.telefono = telefono;
        this.correo = correo;
        this.mfaEnabled = mfaEnabled;
    }

    public Map<String, Object> toMap() {
        return Map.of(
            "numeroDocumento", numeroDocumento != null ? numeroDocumento : "",
            "nombre", nombre != null ? nombre : "",
            "rol", rol != null ? rol : "",
            "telefono", telefono != null ? telefono : "",
            "correo", correo != null ? correo : "",
            "mfaEnabled", mfaEnabled
        );
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public boolean isMfaEnabled() {
        return mfaEnabled;
    }

    public void setMfaEnabled(boolean mfaEnabled) {
        this.mfaEnabled = mfaEnabled;
    }
}