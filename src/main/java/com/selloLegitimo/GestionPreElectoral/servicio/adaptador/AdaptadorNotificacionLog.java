package com.selloLegitimo.GestionPreElectoral.servicio.adaptador;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.selloLegitimo.GestionPreElectoral.servicio.PuertoNotificacionExterna;

@Component
public class AdaptadorNotificacionLog implements PuertoNotificacionExterna {

    private static final Logger logger = LoggerFactory.getLogger(AdaptadorNotificacionLog.class);

    @Override
    public void notificarModificacionListaBlanca(NotificacionModificacionListaBlanca n) {
        // TODO: reemplazar por integraciones REST/mensajería cuando los servicios destino estén disponibles.

        logger.info("[NOTIFICACION->CNE] modificacion emergencia lista blanca ciudadano={} zona {}->{} justificacion='{}' firmantes=[superadmin={}, cne={}] eventoAuditoriaId={} hashEvento={} ts={}",
            n.ciudadanoId(), n.zonaAnterior(), n.zonaNueva(), n.justificacion(),
            n.firmanteSuperadmin(), n.firmanteCne(),
            n.idEventoAuditoria(), n.hashEventoAuditoria(), n.timestamp());

        logger.info("[NOTIFICACION->PROCURADURIA] modificacion emergencia lista blanca ciudadano={} zona {}->{} justificacion='{}' firmantes=[superadmin={}, cne={}] eventoAuditoriaId={} hashEvento={} ts={}",
            n.ciudadanoId(), n.zonaAnterior(), n.zonaNueva(), n.justificacion(),
            n.firmanteSuperadmin(), n.firmanteCne(),
            n.idEventoAuditoria(), n.hashEventoAuditoria(), n.timestamp());

        // SR-M8 (US-SR-M8-01): solo referencia, sin replicar datos sensibles.
        logger.info("[NOTIFICACION->SR-M8] tipologia=MANIPULACION_LISTA_BLANCA eventoAuditoriaId={} hashEvento={} ts={}",
            n.idEventoAuditoria(), n.hashEventoAuditoria(), n.timestamp());
    }
}
