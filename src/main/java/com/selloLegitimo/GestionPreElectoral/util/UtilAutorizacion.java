package com.selloLegitimo.GestionPreElectoral.util;

import com.selloLegitimo.GestionPreElectoral.modelo.RolUsuario;

public class UtilAutorizacion {

    private static final RolUsuario[] ROLES_EXENTOS_MFA = {
        RolUsuario.CIUDADANO,
        RolUsuario.VOTANTE
    };

    public static boolean requiresMFA(String rol) {
        if (rol == null || rol.isBlank()) {
            return false;
        }
        for (RolUsuario exento : ROLES_EXENTOS_MFA) {
            if (exento.name().equalsIgnoreCase(rol)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isMFAExempt(String rol) {
        return !requiresMFA(rol);
    }

    public static boolean isAdminRole(String rol) {
        if (rol == null) return false;
        return rol.equalsIgnoreCase("ADMINISTRADOR") ||
               rol.equalsIgnoreCase("SUPERADMIN") ||
               rol.equalsIgnoreCase("REGISTRADOR");
    }

    public static boolean isCeremonyRole(String rol) {
        if (rol == null) return false;
        return rol.equalsIgnoreCase("MAGISTRADO") ||
               rol.equalsIgnoreCase("SUPERADMIN") ||
               rol.equalsIgnoreCase("CLAVERO");
    }
}