package com.selloLegitimo.GestionPreElectoral.servicio;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.selloLegitimo.GestionPreElectoral.dto.TarjetonEntradaDto;
import com.selloLegitimo.GestionPreElectoral.dto.TarjetonRespuestaDto;
import com.selloLegitimo.GestionPreElectoral.modelo.Candidatura;
import com.selloLegitimo.GestionPreElectoral.modelo.EstadoCandidatura;
import com.selloLegitimo.GestionPreElectoral.modelo.EventoAuditoria;
import com.selloLegitimo.GestionPreElectoral.modelo.TipoEventoAuditoria;
import com.selloLegitimo.GestionPreElectoral.modelo.TipoOrdenamiento;
import com.selloLegitimo.GestionPreElectoral.repositorio.CandidaturaRepositorio;
import com.selloLegitimo.GestionPreElectoral.repositorio.EventoAuditoriaRepositorio;

@Component
public class GeneradorTarjeton {

	private static final Logger logger = LoggerFactory.getLogger(GeneradorTarjeton.class);

	@Autowired
	private CandidaturaRepositorio candidaturaRepositorio;

	@Autowired
	private PuertoAuditoria puertoAuditoria;

	@Autowired
	private EventoAuditoriaRepositorio eventoAuditoriaRepositorio;

	public TarjetonRespuestaDto generar(Long eleccionId, String circunscripcion,
			TipoOrdenamiento tipoOrdenamiento, Long semillaAleatoria, String actor) {

		List<Candidatura> candidaturas = candidaturaRepositorio
				.findByEleccionIdAndEstadoIn(eleccionId, List.of(EstadoCandidatura.APROBADO, EstadoCandidatura.PUBLICADO));

		if (circunscripcion != null && !circunscripcion.isBlank()) {
			candidaturas = candidaturas.stream()
					.filter(c -> circunscripcion.equalsIgnoreCase(c.getCircunscripcion()))
					.toList();
		}

		List<TarjetonEntradaDto> entradas = new ArrayList<>();
		for (Candidatura c : candidaturas) {
			entradas.add(new TarjetonEntradaDto(
					0, c.getNombreCandidato(), c.getPartido(), c.getFotoUrl(), TarjetonEntradaDto.TipoEntradaTarjeton.CANDIDATO));
		}

		// Voto en blanco siempre al final
		entradas.add(new TarjetonEntradaDto(
				0, "Voto en Blanco", null, null, TarjetonEntradaDto.TipoEntradaTarjeton.VOTO_BLANCO));

		Long semillaUsada;
		if (tipoOrdenamiento == TipoOrdenamiento.ALEATORIO_AUDITADO) {
			semillaUsada = semillaAleatoria != null ? semillaAleatoria : System.currentTimeMillis();
			Random random = new Random(semillaUsada);
			Collections.shuffle(entradas.subList(0, entradas.size() - 1), random);
		} else {
			semillaUsada = null;
			entradas.subList(0, entradas.size() - 1).sort(Comparator.comparing(TarjetonEntradaDto::getNombreCandidato, String.CASE_INSENSITIVE_ORDER));
		}

		for (int i = 0; i < entradas.size(); i++) {
			entradas.get(i).setOrden(i + 1);
		}

		TarjetonRespuestaDto respuesta = new TarjetonRespuestaDto(
				eleccionId, circunscripcion, LocalDateTime.now(), semillaUsada, entradas);

		String payload = buildPayload(eleccionId, circunscripcion, tipoOrdenamiento, semillaUsada, entradas);

		puertoAuditoria.registrarEvento(new com.selloLegitimo.GestionPreElectoral.modelo.EventoAuditoria(
				"TARJETON", eleccionId, TipoEventoAuditoria.TARJETON_GENERADO, actor, payload, ""));

		logger.info("Tarjetón generado. eleccion={}, circunscripcion={}, entradas={}, semilla={}",
				eleccionId, circunscripcion, entradas.size(), semillaUsada);

		return respuesta;
	}

	public TarjetonRespuestaDto obtenerUltimoTarjeton(Long eleccionId) {
		Optional<EventoAuditoria> ultimo = eventoAuditoriaRepositorio
				.findFirstByAggregadoTipoAndAggregadoIdAndTipoEventoOrderByIdDesc(
						"TARJETON", eleccionId, TipoEventoAuditoria.TARJETON_GENERADO.name());

		if (ultimo.isPresent()) {
			String payload = ultimo.get().getPayloadJson();
			try {
				ObjectMapper mapper = new ObjectMapper();
				java.util.Map<String, Object> map = mapper.readValue(payload,
						new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, Object>>() {});

				@SuppressWarnings("unchecked")
				List<java.util.Map<String, Object>> entradasRaw = (List<java.util.Map<String, Object>>) map.get("entradas");
				List<TarjetonEntradaDto> entradas = new ArrayList<>();
				if (entradasRaw != null) {
					for (java.util.Map<String, Object> e : entradasRaw) {
						int orden = ((Number) e.get("orden")).intValue();
						String nombre = (String) e.get("nombreCandidato");
						String partido = (String) e.get("partido");
						String fotoUrl = (String) e.get("fotoUrl");
						String tipoStr = (String) e.get("tipo");
						TarjetonEntradaDto.TipoEntradaTarjeton tipo = tipoStr != null
								? TarjetonEntradaDto.TipoEntradaTarjeton.valueOf(tipoStr)
								: TarjetonEntradaDto.TipoEntradaTarjeton.CANDIDATO;
						entradas.add(new TarjetonEntradaDto(orden, nombre, partido, fotoUrl, tipo));
					}
				}

				String circunscripcion = (String) map.get("circunscripcion");
				Number semilla = (Number) map.get("semilla");
				Long semillaLong = semilla != null ? semilla.longValue() : null;

				logger.info("Tarjetón recuperado desde auditoría. eleccion={}, entradas={}", eleccionId, entradas.size());
				return new TarjetonRespuestaDto(eleccionId, circunscripcion,
						ultimo.get().getFechaEvento(), semillaLong, entradas);
			} catch (Exception e) {
				logger.warn("No se pudo deserializar el tarjetón desde auditoría, regenerando", e);
			}
		}

		// Fallback: regenerar on-the-fly si no existe en auditoría
		logger.info("No se encontró tarjetón en auditoría, regenerando on-the-fly. eleccion={}", eleccionId);
		return generar(eleccionId, null, TipoOrdenamiento.ALFABETICO, null, "system-recovery");
	}

	private String buildPayload(Long eleccionId, String circunscripcion,
								TipoOrdenamiento tipoOrdenamiento, Long semillaUsada,
								List<TarjetonEntradaDto> entradas) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			java.util.Map<String, Object> map = new java.util.LinkedHashMap<>();
			map.put("eleccionId", eleccionId);
			map.put("circunscripcion", circunscripcion != null ? circunscripcion : "");
			map.put("tipoOrdenamiento", tipoOrdenamiento.name());
			map.put("semilla", semillaUsada);
			map.put("totalEntradas", entradas.size());
			map.put("entradas", entradas);
			return mapper.writeValueAsString(map);
		} catch (JsonProcessingException e) {
			logger.warn("No se pudo serializar el tarjetón a JSON, usando fallback", e);
			return String.format(
					"{\"eleccionId\":%d,\"circunscripcion\":\"%s\",\"tipoOrdenamiento\":\"%s\",\"semilla\":%s,\"totalEntradas\":%d}",
					eleccionId, circunscripcion != null ? circunscripcion : "",
					tipoOrdenamiento, semillaUsada != null ? semillaUsada : "null", entradas.size());
		}
	}
}
