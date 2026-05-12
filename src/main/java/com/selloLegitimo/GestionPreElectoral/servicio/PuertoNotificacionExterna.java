package com.selloLegitimo.GestionPreElectoral.servicio;

import java.time.LocalDateTime;

public interface PuertoNotificacionExterna {

    void notificarModificacionListaBlanca(NotificacionModificacionListaBlanca notificacion);

    record NotificacionModificacionListaBlanca(
        Long idEventoAuditoria,
        String hashEventoAuditoria,
        String ciudadanoId,
        String zonaAnterior,
        String zonaNueva,
        String justificacion,
        String firmanteSuperadmin,
        String firmanteCne,
        LocalDateTime timestamp
    ) {}
}
