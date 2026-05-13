package com.selloLegitimo.GestionPreElectoral.modelo;

/**
 * Roles de usuario para el sistema de autenticacion.
 * Separacion clara entre roles exentos de MFA y roles que requieren MFA.
 */
public enum RolUsuario {

    CIUDADANO("Ciudadano - exento MFA"),
    VOTANTE("Votante - exento MFA"),
    ADMINISTRADOR("Administrador - requiere MFA"),
    AUDITOR("Auditor - requiere MFA"),
    MAGISTRADO("Magistrado - requiere MFA + acceso boveda"),
    OPERADOR("Operador de mesa - requiere MFA"),
    SUPERADMIN("Superadministrador - requiere MFA"),
    REGISTRADOR("Registrador nacional - requiere MFA"),
    CLAVERO("Clavero - acceso boveda exclusivo");

    private final String descripcion;

    RolUsuario(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}