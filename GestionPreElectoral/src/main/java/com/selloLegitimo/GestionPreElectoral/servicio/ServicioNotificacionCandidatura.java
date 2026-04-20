package com.selloLegitimo.GestionPreElectoral.servicio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.selloLegitimo.GestionPreElectoral.modelo.Candidatura;

@Service
public class ServicioNotificacionCandidatura {

	private static final Logger logger = LoggerFactory.getLogger(ServicioNotificacionCandidatura.class);

	public void notificarCambio(Candidatura candidatura, String evento) {
		logger.info("Notificación automática generada. candidatura={}, partido={}, candidato={}, evento={}",
				candidatura.getId(), candidatura.getPartido(), candidatura.getNombreCandidato(), evento);
	}
}