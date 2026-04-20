package com.selloLegitimo.GestionPreElectoral.servicio;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.selloLegitimo.GestionPreElectoral.dto.ActualizarDatosCandidaturaSolicitudDto;
import com.selloLegitimo.GestionPreElectoral.dto.ActualizarEstadoCandidaturaSolicitudDto;
import com.selloLegitimo.GestionPreElectoral.dto.CandidaturaRespuestaDto;
import com.selloLegitimo.GestionPreElectoral.dto.DetalleEleccionExternaDto;
import com.selloLegitimo.GestionPreElectoral.dto.RegistrarCandidaturaSolicitudDto;
import com.selloLegitimo.GestionPreElectoral.dto.ReemplazarCandidaturaSolicitudDto;
import com.selloLegitimo.GestionPreElectoral.dto.ResultadoValidacionInhabilidadesDto;
import com.selloLegitimo.GestionPreElectoral.excepcion.ExcepcionNegocio;
import com.selloLegitimo.GestionPreElectoral.excepcion.RecursoNoEncontradoExcepcion;
import com.selloLegitimo.GestionPreElectoral.modelo.Candidatura;
import com.selloLegitimo.GestionPreElectoral.modelo.EstadoCandidatura;
import com.selloLegitimo.GestionPreElectoral.modelo.MotivoReemplazoCandidatura;
import com.selloLegitimo.GestionPreElectoral.repositorio.CandidaturaRepositorio;

@Service
public class ServicioCandidatura {

	private static final Logger logger = LoggerFactory.getLogger(ServicioCandidatura.class);

	@Autowired
	private CandidaturaRepositorio candidaturaRepositorio;

	@Autowired
	private ServicioEleccion servicioEleccion;

	@Autowired
	private ServicioNotificacionCandidatura servicioNotificacionCandidatura;

	@Transactional
	public CandidaturaRespuestaDto registrar(RegistrarCandidaturaSolicitudDto solicitud) {
		DetalleEleccionExternaDto eleccion = servicioEleccion.obtenerEleccion(solicitud.getEleccionId());
		validarPeriodoModificacion(eleccion);
		validarDocumentoUnico(eleccion.getId(), solicitud.getDocumento(), null);

		Candidatura candidatura = new Candidatura(eleccion.getId(), solicitud.getNombreCandidato(), solicitud.getDocumento(),
				solicitud.getPartido(), solicitud.getCircunscripcion(), solicitud.getFotoUrl(), EstadoCandidatura.INSCRITA,
				solicitud.getActor());
		candidaturaRepositorio.save(candidatura);
		servicioNotificacionCandidatura.notificarCambio(candidatura, "Candidatura inscrita");
		logger.info("Candidatura registrada. id={}, elección={}, documento={}", candidatura.getId(), eleccion.getId(), solicitud.getDocumento());
		return mapear(candidatura);
	}

	@Transactional
	public CandidaturaRespuestaDto actualizarDatos(Long candidaturaId, ActualizarDatosCandidaturaSolicitudDto solicitud) {
		Candidatura candidatura = obtenerEntidad(candidaturaId);
		DetalleEleccionExternaDto eleccion = servicioEleccion.obtenerEleccion(candidatura.getEleccionId());
		validarPeriodoModificacion(eleccion);
		validarEstadoEditable(candidatura.getEstado());
		candidatura.actualizarDatos(solicitud.getNombreCandidato(), solicitud.getPartido(), solicitud.getCircunscripcion(),
				solicitud.getFotoUrl(), solicitud.getActor());
		servicioNotificacionCandidatura.notificarCambio(candidatura, "Datos de candidatura actualizados");
		logger.info("Datos de candidatura actualizados. candidatura={}, actor={}", candidaturaId, solicitud.getActor());
		return mapear(candidatura);
	}

	@Transactional
	public CandidaturaRespuestaDto actualizarEstado(Long candidaturaId, ActualizarEstadoCandidaturaSolicitudDto solicitud) {
		Candidatura candidatura = obtenerEntidad(candidaturaId);
		validarTransicionEstado(candidatura.getEstado(), solicitud.getEstado());
		candidatura.actualizarEstado(solicitud.getEstado(), solicitud.getActor());
		servicioNotificacionCandidatura.notificarCambio(candidatura,
				"Estado de candidatura cambiado a " + solicitud.getEstado().name());
		logger.info("Estado de candidatura actualizado. candidatura={}, estado={}, actor={}", candidaturaId, solicitud.getEstado(), solicitud.getActor());
		return mapear(candidatura);
	}

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
		Candidatura nuevaCandidatura = new Candidatura(eleccion.getId(), solicitud.getNombreCandidato(), solicitud.getDocumento(),
				solicitud.getPartido(), solicitud.getCircunscripcion(), solicitud.getFotoUrl(), EstadoCandidatura.INSCRITA,
				solicitud.getActor());
		nuevaCandidatura.definirOrigenReemplazo(candidaturaOriginal, solicitud.getMotivoReemplazo(),
				solicitud.getJustificacionReemplazo(), solicitud.getActor());
		candidaturaRepositorio.save(nuevaCandidatura);
		candidaturaOriginal.marcarComoReemplazada(nuevaCandidatura, solicitud.getMotivoReemplazo(),
				solicitud.getJustificacionReemplazo(), solicitud.getActor());
		servicioNotificacionCandidatura.notificarCambio(candidaturaOriginal, "Candidatura reemplazada");
		servicioNotificacionCandidatura.notificarCambio(nuevaCandidatura, "Candidatura de reemplazo inscrita");
		logger.info("Candidatura reemplazada. original={}, nueva={}, actor={}", candidaturaId, nuevaCandidatura.getId(), solicitud.getActor());
		return mapear(nuevaCandidatura);
	}

	@Transactional(readOnly = true)
	public List<CandidaturaRespuestaDto> listarPorEleccion(Long eleccionId) {
		return candidaturaRepositorio.findByEleccionIdOrderByFechaInscripcionDesc(eleccionId)
			.stream()
			.map(this::mapear)
			.toList();
	}

	@Transactional(readOnly = true)
	public ResultadoValidacionInhabilidadesDto validarInhabilidades(Long candidaturaId, String endpointExterno) {
		obtenerEntidad(candidaturaId);
		logger.info("Endpoint de validación de inhabilidades invocado. candidatura={}, endpointExterno={}", candidaturaId,
				endpointExterno);
		return new ResultadoValidacionInhabilidadesDto(candidaturaId,
				"Endpoint disponible para integración con Procuraduría y CNE", endpointExterno);
	}

	@Transactional(readOnly = true)
	public Candidatura obtenerEntidad(Long candidaturaId) {
		return candidaturaRepositorio.findById(candidaturaId)
			.orElseThrow(() -> new RecursoNoEncontradoExcepcion("No existe la candidatura con id " + candidaturaId));
	}

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
		if (EstadoCandidatura.REEMPLAZADA.equals(estadoActual) || EstadoCandidatura.REVOCADA.equals(estadoActual)) {
			throw new ExcepcionNegocio("La candidatura no puede ser modificada en su estado actual");
		}
	}

	private void validarTransicionEstado(EstadoCandidatura estadoActual, EstadoCandidatura nuevoEstado) {
		if (estadoActual.equals(nuevoEstado)) {
			return;
		}

		boolean transicionValida = switch (estadoActual) {
			case INSCRITA -> EstadoCandidatura.EN_REVISION.equals(nuevoEstado);
			case EN_REVISION -> EstadoCandidatura.ACEPTADA.equals(nuevoEstado)
					|| EstadoCandidatura.RECHAZADA.equals(nuevoEstado);
			case ACEPTADA -> EstadoCandidatura.REVOCADA.equals(nuevoEstado);
			case RECHAZADA, REVOCADA, REEMPLAZADA -> false;
		};

		if (!transicionValida) {
			throw new ExcepcionNegocio("La transición de estado de candidatura no es válida");
		}
	}

	private CandidaturaRespuestaDto mapear(Candidatura candidatura) {
		Long candidaturaReemplazadaId = candidatura.getCandidaturaReemplazada() != null
				? candidatura.getCandidaturaReemplazada().getId()
				: null;
		return new CandidaturaRespuestaDto(candidatura.getId(), candidatura.getEleccionId(),
				candidatura.getNombreCandidato(), candidatura.getDocumento(), candidatura.getPartido(),
				candidatura.getCircunscripcion(), candidatura.getFotoUrl(), candidatura.getEstado(),
				candidaturaReemplazadaId, candidatura.getMotivoReemplazo(), candidatura.getJustificacionReemplazo(),
				candidatura.getActorUltimaModificacion(), candidatura.getFechaInscripcion(),
				candidatura.getFechaActualizacion());
	}
}