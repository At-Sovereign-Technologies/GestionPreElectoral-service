package com.selloLegitimo.GestionPreElectoral.servicio;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.selloLegitimo.GestionPreElectoral.dto.ActualizarDatosCandidaturaSolicitudDto;
import com.selloLegitimo.GestionPreElectoral.dto.ActualizarEstadoCandidaturaSolicitudDto;
import com.selloLegitimo.GestionPreElectoral.dto.CandidaturaRespuestaDto;
import com.selloLegitimo.GestionPreElectoral.dto.ContextoValidacionDto;
import com.selloLegitimo.GestionPreElectoral.dto.DetalleEleccionExternaDto;
import com.selloLegitimo.GestionPreElectoral.dto.GenerarTarjetonSolicitudDto;
import com.selloLegitimo.GestionPreElectoral.dto.RegistrarCandidaturaSolicitudDto;
import com.selloLegitimo.GestionPreElectoral.dto.ReemplazarCandidaturaSolicitudDto;
import com.selloLegitimo.GestionPreElectoral.dto.ResultadoValidacionInhabilidadesDto;
import com.selloLegitimo.GestionPreElectoral.dto.TarjetonRespuestaDto;
import com.selloLegitimo.GestionPreElectoral.dto.TransicionEstadoSolicitudDto;
import com.selloLegitimo.GestionPreElectoral.excepcion.ExcepcionNegocio;
import com.selloLegitimo.GestionPreElectoral.excepcion.RecursoNoEncontradoExcepcion;
import com.selloLegitimo.GestionPreElectoral.modelo.Candidatura;
import com.selloLegitimo.GestionPreElectoral.modelo.CandidaturaVersion;
import com.selloLegitimo.GestionPreElectoral.modelo.EstadoCandidatura;
import com.selloLegitimo.GestionPreElectoral.modelo.EventoAuditoria;
import com.selloLegitimo.GestionPreElectoral.modelo.MotivoReemplazoCandidatura;
import com.selloLegitimo.GestionPreElectoral.modelo.TipoEventoAuditoria;
import com.selloLegitimo.GestionPreElectoral.repositorio.CandidaturaRepositorio;
import com.selloLegitimo.GestionPreElectoral.repositorio.CandidaturaVersionRepositorio;

@Service
public class ServicioCandidatura {

	private static final Logger logger = LoggerFactory.getLogger(ServicioCandidatura.class);
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	@Autowired
	private CandidaturaRepositorio candidaturaRepositorio;

	@Autowired
	private CandidaturaVersionRepositorio candidaturaVersionRepositorio;

	@Autowired
	private ServicioEleccion servicioEleccion;

	@Autowired
	private ServicioNotificacionCandidatura servicioNotificacionCandidatura;

	@Autowired
	private MotorValidacionInhabilidades motorValidacionInhabilidades;

	@Autowired
	private PuertoAuditoria puertoAuditoria;

	@Autowired
	private GeneradorTarjeton generadorTarjeton;

	// =========================
	// REGISTRO / POSTULACIÓN
	// =========================

	@Transactional
	public CandidaturaRespuestaDto registrar(RegistrarCandidaturaSolicitudDto solicitud) {
		DetalleEleccionExternaDto eleccion = servicioEleccion.obtenerEleccion(solicitud.getEleccionId());
		validarPeriodoModificacion(eleccion);
		validarDocumentoUnico(eleccion.getId(), solicitud.getDocumento(), null);

		Candidatura candidatura = new Candidatura(
				eleccion.getId(), solicitud.getNombreCandidato(), solicitud.getDocumento(),
				solicitud.getPartido(), solicitud.getCircunscripcion(), solicitud.getFotoUrl(),
				EstadoCandidatura.POSTULADO, solicitud.getActor());
		candidaturaRepositorio.save(candidatura);

		registrarAuditoria(candidatura, TipoEventoAuditoria.CANDIDATURA_POSTULADA, solicitud.getActor(), null);
		servicioNotificacionCandidatura.notificarCambio(candidatura, "Candidatura postulada");
		logger.info("Candidatura registrada. id={}, elección={}, documento={}", candidatura.getId(), eleccion.getId(), solicitud.getDocumento());
		return mapear(candidatura);
	}

	// =========================
	// ACTUALIZACIÓN DE DATOS
	// =========================

	@Transactional
	public CandidaturaRespuestaDto actualizarDatos(Long candidaturaId, ActualizarDatosCandidaturaSolicitudDto solicitud) {
		Candidatura candidatura = obtenerEntidad(candidaturaId);
		DetalleEleccionExternaDto eleccion = servicioEleccion.obtenerEleccion(candidatura.getEleccionId());
		validarPeriodoModificacion(eleccion);
		validarEstadoEditable(candidatura.getEstado());

		guardarVersion(candidatura, solicitud.getActor());
		candidatura.actualizarDatos(solicitud.getNombreCandidato(), solicitud.getPartido(), solicitud.getCircunscripcion(),
				solicitud.getFotoUrl(), solicitud.getActor());

		registrarAuditoria(candidatura, TipoEventoAuditoria.CANDIDATURA_ACTUALIZADA, solicitud.getActor(), null);
		servicioNotificacionCandidatura.notificarCambio(candidatura, "Datos de candidatura actualizados");
		logger.info("Datos de candidatura actualizados. candidatura={}, actor={}", candidaturaId, solicitud.getActor());
		return mapear(candidatura);
	}

	// =========================
	// TRANSICIÓN DE ESTADO (state machine)
	// =========================

	@Transactional
	public CandidaturaRespuestaDto transicionarEstado(Long candidaturaId, TransicionEstadoSolicitudDto solicitud) {
		Candidatura candidatura = obtenerEntidad(candidaturaId);
		EstadoCandidatura estadoActual = candidatura.getEstado();
		EstadoCandidatura nuevoEstado = solicitud.getEstado();

		validarTransicionEstado(estadoActual, nuevoEstado);
		validarPermisoTransicion(solicitud.getRol(), estadoActual, nuevoEstado);

		// Validaciones síncronas antes de transiciones críticas
		if (nuevoEstado == EstadoCandidatura.EN_VALIDACION || nuevoEstado == EstadoCandidatura.APROBADO) {
			DetalleEleccionExternaDto eleccion = servicioEleccion.obtenerEleccion(candidatura.getEleccionId());
			ContextoValidacionDto ctx = new ContextoValidacionDto(eleccion, LocalDateTime.now());
			ResultadoValidacionInhabilidadesDto resultado = motorValidacionInhabilidades.validar(candidatura, ctx);
			if (!resultado.isValido()) {
				throw new ExcepcionNegocio("No se puede transicionar el estado: existen inhabilidades bloqueantes");
			}
		}

		guardarVersion(candidatura, solicitud.getActor());
		candidatura.actualizarEstado(nuevoEstado, solicitud.getActor());

		TipoEventoAuditoria tipoEvento = mapearEventoPorEstado(nuevoEstado);
		String payload = construirPayloadJustificacion(solicitud.getJustificacion());
		registrarAuditoria(candidatura, tipoEvento, solicitud.getActor(), payload);

		servicioNotificacionCandidatura.notificarCambio(candidatura,
				"Estado de candidatura cambiado a " + nuevoEstado.name());
		logger.info("Estado de candidatura actualizado. candidatura={}, estado={}, actor={}",
				candidaturaId, nuevoEstado, solicitud.getActor());
		return mapear(candidatura);
	}

	/**
	 * Método legacy compatibilidad con ActualizarEstadoCandidaturaSolicitudDto (sin rol).
	 */
	@Transactional
	public CandidaturaRespuestaDto actualizarEstado(Long candidaturaId, ActualizarEstadoCandidaturaSolicitudDto solicitud) {
		TransicionEstadoSolicitudDto transicion = new TransicionEstadoSolicitudDto();
		transicion.setEstado(solicitud.getEstado());
		transicion.setActor(solicitud.getActor());
		transicion.setJustificacion(solicitud.getJustificacion());
		transicion.setRol("REGISTRADURIA"); // fallback para compatibilidad
		return transicionarEstado(candidaturaId, transicion);
	}

	// =========================
	// REEMPLAZO
	// =========================

	@Transactional
	public CandidaturaRespuestaDto reemplazar(Long candidaturaId, ReemplazarCandidaturaSolicitudDto solicitud) {
		Candidatura candidaturaOriginal = obtenerEntidad(candidaturaId);
		DetalleEleccionExternaDto eleccion = servicioEleccion.obtenerEleccion(candidaturaOriginal.getEleccionId());
		if (!eleccion.permiteReemplazoCandidaturas()) {
			throw new ExcepcionNegocio("La fecha límite para reemplazo de candidaturas ya fue superada");
		}
		if (!MotivoReemplazoCandidatura.MUERTE.equals(solicitud.getMotivoReemplazo())
				&& !MotivoReemplazoCandidatura.INCAPACIDAD.equals(solicitud.getMotivoReemplazo())) {
			throw new ExcepcionNegocio("El reemplazo solo procede por muerte o incapacidad");
		}
		if (EstadoCandidatura.REEMPLAZADA.equals(candidaturaOriginal.getEstado())) {
			throw new ExcepcionNegocio("La candidatura ya fue reemplazada previamente");
		}

		validarDocumentoUnico(eleccion.getId(), solicitud.getDocumento(), null);
		Candidatura nuevaCandidatura = new Candidatura(
				eleccion.getId(), solicitud.getNombreCandidato(), solicitud.getDocumento(),
				solicitud.getPartido(), solicitud.getCircunscripcion(), solicitud.getFotoUrl(),
				EstadoCandidatura.POSTULADO, solicitud.getActor());
		nuevaCandidatura.definirOrigenReemplazo(candidaturaOriginal, solicitud.getMotivoReemplazo(),
				solicitud.getJustificacionReemplazo(), solicitud.getActor());
		candidaturaRepositorio.save(nuevaCandidatura);
		candidaturaOriginal.marcarComoReemplazada(nuevaCandidatura, solicitud.getMotivoReemplazo(),
				solicitud.getJustificacionReemplazo(), solicitud.getActor());

		registrarAuditoria(candidaturaOriginal, TipoEventoAuditoria.CANDIDATURA_REEMPLAZADA, solicitud.getActor(), null);
		servicioNotificacionCandidatura.notificarCambio(candidaturaOriginal, "Candidatura reemplazada");
		servicioNotificacionCandidatura.notificarCambio(nuevaCandidatura, "Candidatura de reemplazo inscrita");
		logger.info("Candidatura reemplazada. original={}, nueva={}, actor={}", candidaturaId, nuevaCandidatura.getId(), solicitud.getActor());
		return mapear(nuevaCandidatura);
	}

	// =========================
	// VALIDACIÓN DE INHABILIDADES
	// =========================

	@Transactional(readOnly = true)
	public ResultadoValidacionInhabilidadesDto validarInhabilidades(Long candidaturaId, String endpointExterno) {
		Candidatura candidatura = obtenerEntidad(candidaturaId);
		DetalleEleccionExternaDto eleccion = servicioEleccion.obtenerEleccion(candidatura.getEleccionId());
		ContextoValidacionDto ctx = new ContextoValidacionDto(eleccion, LocalDateTime.now());
		ResultadoValidacionInhabilidadesDto resultado = motorValidacionInhabilidades.validar(candidatura, ctx);
		resultado.setEndpointExterno(endpointExterno);
		logger.info("Validación de inhabilidades ejecutada. candidatura={}, valido={}, violaciones={}",
				candidaturaId, resultado.isValido(), resultado.getViolaciones().size());
		return resultado;
	}

	// =========================
	// CONSULTAS
	// =========================

	@Transactional(readOnly = true)
	public List<CandidaturaRespuestaDto> listarPorEleccion(Long eleccionId) {
		return candidaturaRepositorio.findByEleccionIdOrderByFechaInscripcionDesc(eleccionId)
				.stream()
				.map(this::mapear)
				.toList();
	}

	@Transactional(readOnly = true)
	public Candidatura obtenerEntidad(Long candidaturaId) {
		return candidaturaRepositorio.findById(candidaturaId)
				.orElseThrow(() -> new RecursoNoEncontradoExcepcion("No existe la candidatura con id " + candidaturaId));
	}

	@Transactional(readOnly = true)
	public List<CandidaturaVersion> listarVersiones(Long candidaturaId) {
		obtenerEntidad(candidaturaId); // validar existencia
		return candidaturaVersionRepositorio.findByCandidaturaIdOrderByVersionNumberDesc(candidaturaId);
	}

	// =========================
	// TARJETÓN
	// =========================

	@Transactional
	public TarjetonRespuestaDto generarTarjeton(Long eleccionId, GenerarTarjetonSolicitudDto solicitud) {
		return generadorTarjeton.generar(
				eleccionId, solicitud.getCircunscripcion(),
				solicitud.getTipoOrdenamiento(), solicitud.getSemillaAleatoria(), solicitud.getActor());
	}

	@Transactional(readOnly = true)
	public TarjetonRespuestaDto obtenerUltimoTarjeton(Long eleccionId) {
		return generadorTarjeton.obtenerUltimoTarjeton(eleccionId);
	}

	// =========================
	// AUXILIARES
	// =========================

	private void validarPeriodoModificacion(DetalleEleccionExternaDto eleccion) {
		if (!eleccion.estaDentroPeriodoModificacionCandidaturas()) {
			throw new ExcepcionNegocio("La elección no se encuentra dentro del período de modificación de candidaturas");
		}
	}

	private void validarDocumentoUnico(Long eleccionId, String documento, Long candidaturaIdActual) {
		boolean existe = candidaturaIdActual == null
				? candidaturaRepositorio.existsByEleccionIdAndDocumento(eleccionId, documento)
				: candidaturaRepositorio.existsByEleccionIdAndDocumentoAndIdNot(eleccionId, documento, candidaturaIdActual);
		if (existe) {
			throw new ExcepcionNegocio("Ya existe una candidatura registrada con ese documento en la elección");
		}
	}

	private void validarEstadoEditable(EstadoCandidatura estadoActual) {
		if (estadoActual == EstadoCandidatura.REEMPLAZADA || estadoActual == EstadoCandidatura.REVOCADA
				|| estadoActual == EstadoCandidatura.BLOQUEADO || estadoActual == EstadoCandidatura.RECHAZADO) {
			throw new ExcepcionNegocio("La candidatura no puede ser modificada en su estado actual");
		}
	}

	private void validarTransicionEstado(EstadoCandidatura estadoActual, EstadoCandidatura nuevoEstado) {
		if (estadoActual.equals(nuevoEstado)) {
			return;
		}

		boolean transicionValida = switch (estadoActual) {
			case BORRADOR -> nuevoEstado == EstadoCandidatura.POSTULADO || nuevoEstado == EstadoCandidatura.RECHAZADO;
			case POSTULADO -> nuevoEstado == EstadoCandidatura.EN_VALIDACION || nuevoEstado == EstadoCandidatura.RECHAZADO;
			case EN_VALIDACION -> nuevoEstado == EstadoCandidatura.APROBADO || nuevoEstado == EstadoCandidatura.RECHAZADO;
			case APROBADO -> nuevoEstado == EstadoCandidatura.PUBLICADO || nuevoEstado == EstadoCandidatura.RECHAZADO;
			case PUBLICADO -> nuevoEstado == EstadoCandidatura.BLOQUEADO;
			case RECHAZADO, BLOQUEADO, REEMPLAZADA, REVOCADA -> false;
		};

		if (!transicionValida) {
			throw new ExcepcionNegocio("La transición de estado de candidatura no es válida");
		}
	}

	private void validarPermisoTransicion(String rol, EstadoCandidatura estadoActual, EstadoCandidatura nuevoEstado) {
		// Transiciones que solo puede hacer REGISTRADURIA
		boolean requiereRegistraduria = switch (nuevoEstado) {
			case EN_VALIDACION, APROBADO, RECHAZADO, PUBLICADO, BLOQUEADO -> true;
			default -> false;
		};

		if (requiereRegistraduria && !"REGISTRADURIA".equalsIgnoreCase(rol)) {
			throw new ExcepcionNegocio("No tiene permisos para realizar esta transición de estado");
		}

		// POSTULADO puede ser alcanzado desde BORRADOR por PARTIDO/CANDIDATO_IND
		if (estadoActual == EstadoCandidatura.BORRADOR && nuevoEstado == EstadoCandidatura.POSTULADO) {
			if (!"PARTIDO".equalsIgnoreCase(rol) && !"CANDIDATO_IND".equalsIgnoreCase(rol)) {
				throw new ExcepcionNegocio("No tiene permisos para realizar esta transición de estado");
			}
		}
	}

	private void guardarVersion(Candidatura candidatura, String actor) {
		Long versionActual = candidatura.getVersion() != null ? candidatura.getVersion() : 0L;
		CandidaturaVersion version = new CandidaturaVersion(candidatura, versionActual, actor);
		candidaturaVersionRepositorio.save(version);
	}

	private TipoEventoAuditoria mapearEventoPorEstado(EstadoCandidatura estado) {
		return switch (estado) {
			case APROBADO -> TipoEventoAuditoria.CANDIDATURA_APROBADA;
			case RECHAZADO -> TipoEventoAuditoria.CANDIDATURA_RECHAZADA;
			case PUBLICADO -> TipoEventoAuditoria.CANDIDATURA_PUBLICADA;
			default -> TipoEventoAuditoria.CANDIDATURA_ESTADO_CAMBIADO;
		};
	}

	private String construirPayloadJustificacion(String justificacion) {
		if (justificacion == null || justificacion.isBlank()) {
			return null;
		}
		try {
			return OBJECT_MAPPER.writeValueAsString(java.util.Map.of("justificacion", justificacion));
		} catch (JsonProcessingException e) {
			return "{\"justificacion\":\"" + justificacion.replace("\"", "\\\"") + "\"}";
		}
	}

	private void registrarAuditoria(Candidatura candidatura, TipoEventoAuditoria tipoEvento, String actor, String payload) {
		EventoAuditoria evento = new EventoAuditoria(
				"CANDIDATURA", candidatura.getId(), tipoEvento, actor, payload, "");
		puertoAuditoria.registrarEvento(evento);
	}

	private CandidaturaRespuestaDto mapear(Candidatura candidatura) {
		Long candidaturaReemplazadaId = candidatura.getCandidaturaReemplazada() != null
				? candidatura.getCandidaturaReemplazada().getId()
				: null;
		return new CandidaturaRespuestaDto(
				candidatura.getId(), candidatura.getEleccionId(),
				candidatura.getNombreCandidato(), candidatura.getDocumento(), candidatura.getPartido(),
				candidatura.getCircunscripcion(), candidatura.getFotoUrl(), candidatura.getEstado(),
				candidaturaReemplazadaId, candidatura.getMotivoReemplazo(), candidatura.getJustificacionReemplazo(),
				candidatura.getActorUltimaModificacion(), candidatura.getFechaInscripcion(),
				candidatura.getFechaActualizacion(), candidatura.getVersion());
	}
}
