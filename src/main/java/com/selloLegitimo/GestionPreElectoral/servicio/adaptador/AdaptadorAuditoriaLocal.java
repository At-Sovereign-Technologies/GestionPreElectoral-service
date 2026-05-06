package com.selloLegitimo.GestionPreElectoral.servicio.adaptador;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.selloLegitimo.GestionPreElectoral.modelo.EventoAuditoria;
import com.selloLegitimo.GestionPreElectoral.repositorio.EventoAuditoriaRepositorio;
import com.selloLegitimo.GestionPreElectoral.servicio.PuertoAuditoria;

@Component
public class AdaptadorAuditoriaLocal implements PuertoAuditoria {

	private static final Logger logger = LoggerFactory.getLogger(AdaptadorAuditoriaLocal.class);
	private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

	@Autowired
	private EventoAuditoriaRepositorio eventoAuditoriaRepositorio;

	@Override
	@Transactional
	public void registrarEvento(EventoAuditoria evento) {
		String hash = calcularHash(evento);
		evento.setHashIntegridad(hash);
		eventoAuditoriaRepositorio.save(evento);
		logger.info("[AUDITORIA] {} - aggregado={} id={} actor={}",
			evento.getTipoEvento(), evento.getAggregadoTipo(), evento.getAggregadoId(), evento.getActor());
	}

	private String calcularHash(EventoAuditoria evento) {
		StringBuilder sb = new StringBuilder();
		sb.append(evento.getAggregadoTipo())
		  .append(evento.getAggregadoId())
		  .append(evento.getTipoEvento())
		  .append(evento.getActor())
		  .append(evento.getFechaEvento() != null ? evento.getFechaEvento().format(FMT) : "");

		EventoAuditoria anterior = eventoAuditoriaRepositorio.findFirstByOrderByIdDesc().orElse(null);

		if (anterior != null) {
			sb.append(anterior.getHashIntegridad());
		}

		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(hash);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 no disponible", e);
		}
	}
}
