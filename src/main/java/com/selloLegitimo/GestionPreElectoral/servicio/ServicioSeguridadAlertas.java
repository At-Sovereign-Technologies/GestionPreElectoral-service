package com.selloLegitimo.GestionPreElectoral.servicio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ServicioSeguridadAlertas {

    private static final Logger logger = LoggerFactory.getLogger(ServicioSeguridadAlertas.class);

    public void alertFailedAttempts(String userId, int attempts) {
        logger.warn("[SEGURIDAD-ALERTA] Intento de login fallido critico | userId={} attempts={}",
            userId, attempts);
    }

    public void alertAnomalousIP(String userId, String ip) {
        logger.warn("[SEGURIDAD-ALERTA] Login desde IP no habitual | userId={} ip={}",
            userId, ip);
    }
}