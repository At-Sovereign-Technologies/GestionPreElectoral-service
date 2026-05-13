package com.selloLegitimo.GestionPreElectoral.servicio;

import org.springframework.stereotype.Service;

@Service
public class ServicioAuditoriaStub {

    public void logAuthEvent(String actorId, String action, String entityType,
                             String entityId, String ipAddress) {
        System.out.println("[AUDIT] action=" + action + " actor=" + actorId +
            " entity=" + entityType + ":" + entityId + " ip=" + ipAddress);
    }
}