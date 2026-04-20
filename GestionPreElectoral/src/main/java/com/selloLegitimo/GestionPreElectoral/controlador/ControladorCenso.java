package com.selloLegitimo.GestionPreElectoral.controlador;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.selloLegitimo.GestionPreElectoral.dto.ActualizarEstadoCensoSolicitudDto;
import com.selloLegitimo.GestionPreElectoral.dto.AutorizacionCierreSolicitudDto;
import com.selloLegitimo.GestionPreElectoral.dto.EleccionResumenDto;
import com.selloLegitimo.GestionPreElectoral.dto.ImportarCensoApiSolicitudDto;
import com.selloLegitimo.GestionPreElectoral.dto.MensajeRespuestaDto;
import com.selloLegitimo.GestionPreElectoral.dto.RegistrarCiudadanoCensoSolicitudDto;
import com.selloLegitimo.GestionPreElectoral.dto.RegistroCensoRespuestaDto;
import com.selloLegitimo.GestionPreElectoral.servicio.ServicioCenso;
import com.selloLegitimo.GestionPreElectoral.servicio.ServicioEleccion;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/censo")
public class ControladorCenso {

	@Autowired
	private ServicioCenso servicioCenso;

	@Autowired
	private ServicioEleccion servicioEleccion;

	@GetMapping("/ping")
	public MensajeRespuestaDto ping() {
		return new MensajeRespuestaDto("Servicio de gestión de censo disponible");
	}

	@GetMapping("/elecciones")
	public List<EleccionResumenDto> listarElecciones() {
		return servicioEleccion.listarElecciones();
	}

	@PostMapping("/registros")
	public RegistroCensoRespuestaDto registrarManual(@Valid @RequestBody RegistrarCiudadanoCensoSolicitudDto solicitud) {
		return servicioCenso.registrarManual(solicitud);
	}

	@PutMapping("/registros/{registroId}")
	public RegistroCensoRespuestaDto actualizarEstado(@PathVariable Long registroId,
			@Valid @RequestBody ActualizarEstadoCensoSolicitudDto solicitud) {
		return servicioCenso.actualizarEstado(registroId, solicitud);
	}

	@GetMapping("/elecciones/{eleccionId}/registros")
	public List<RegistroCensoRespuestaDto> listarPorEleccion(@PathVariable Long eleccionId) {
		return servicioCenso.listarPorEleccion(eleccionId);
	}

	@PostMapping("/importaciones/csv")
	public MensajeRespuestaDto importarCsv(@RequestParam Long eleccionId, @RequestParam String actor,
			@RequestParam(required = false) String superadministrador,
			@RequestParam(required = false) String justificacion,
			@RequestParam MultipartFile archivo) {
		AutorizacionCierreSolicitudDto autorizacion = construirAutorizacion(superadministrador, justificacion);
		int total = servicioCenso.importarCsv(eleccionId, archivo, actor, autorizacion);
		return new MensajeRespuestaDto("Importación CSV completada con " + total + " registros");
	}

	@PostMapping("/importaciones/api")
	public MensajeRespuestaDto importarApi(@Valid @RequestBody ImportarCensoApiSolicitudDto solicitud) {
		int total = servicioCenso.importarApi(solicitud);
		return new MensajeRespuestaDto("Importación API completada con " + total + " registros");
	}

	private AutorizacionCierreSolicitudDto construirAutorizacion(String superadministrador, String justificacion) {
		if ((superadministrador == null || superadministrador.isBlank())
				&& (justificacion == null || justificacion.isBlank())) {
			return null;
		}
		return new AutorizacionCierreSolicitudDto(superadministrador, justificacion);
	}
}