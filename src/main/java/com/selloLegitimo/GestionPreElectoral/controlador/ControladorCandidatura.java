package com.selloLegitimo.GestionPreElectoral.controlador;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

import com.selloLegitimo.GestionPreElectoral.dto.ActualizarDatosCandidaturaSolicitudDto;
import com.selloLegitimo.GestionPreElectoral.dto.ActualizarEstadoCandidaturaSolicitudDto;
import com.selloLegitimo.GestionPreElectoral.dto.CandidaturaRespuestaDto;
import com.selloLegitimo.GestionPreElectoral.dto.GenerarTarjetonSolicitudDto;
import com.selloLegitimo.GestionPreElectoral.dto.RegistrarCandidaturaSolicitudDto;
import com.selloLegitimo.GestionPreElectoral.dto.ReemplazarCandidaturaSolicitudDto;
import com.selloLegitimo.GestionPreElectoral.dto.ResultadoValidacionInhabilidadesDto;
import com.selloLegitimo.GestionPreElectoral.dto.TarjetonRespuestaDto;
import com.selloLegitimo.GestionPreElectoral.dto.TransicionEstadoSolicitudDto;
import com.selloLegitimo.GestionPreElectoral.modelo.CandidaturaVersion;
import com.selloLegitimo.GestionPreElectoral.servicio.ServicioAlmacenamientoFotos;
import com.selloLegitimo.GestionPreElectoral.servicio.ServicioCandidatura;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/candidaturas")
public class ControladorCandidatura {

	@Autowired
	private ServicioCandidatura servicioCandidatura;

	@Autowired
	private ServicioAlmacenamientoFotos servicioAlmacenamientoFotos;

	@PostMapping
	public CandidaturaRespuestaDto registrar(@Valid @RequestBody RegistrarCandidaturaSolicitudDto solicitud) {
		return servicioCandidatura.registrar(solicitud);
	}

	@PutMapping("/{candidaturaId}/datos")
	public CandidaturaRespuestaDto actualizarDatos(@PathVariable Long candidaturaId,
			@Valid @RequestBody ActualizarDatosCandidaturaSolicitudDto solicitud) {
		return servicioCandidatura.actualizarDatos(candidaturaId, solicitud);
	}

	@PutMapping("/{candidaturaId}/estado")
	public CandidaturaRespuestaDto actualizarEstado(@PathVariable Long candidaturaId,
			@Valid @RequestBody ActualizarEstadoCandidaturaSolicitudDto solicitud) {
		return servicioCandidatura.actualizarEstado(candidaturaId, solicitud);
	}

	@PutMapping("/{candidaturaId}/transicion")
	public CandidaturaRespuestaDto transicionarEstado(@PathVariable Long candidaturaId,
			@Valid @RequestBody TransicionEstadoSolicitudDto solicitud) {
		return servicioCandidatura.transicionarEstado(candidaturaId, solicitud);
	}

	@PostMapping("/{candidaturaId}/reemplazo")
	public CandidaturaRespuestaDto reemplazar(@PathVariable Long candidaturaId,
			@Valid @RequestBody ReemplazarCandidaturaSolicitudDto solicitud) {
		return servicioCandidatura.reemplazar(candidaturaId, solicitud);
	}

	@PostMapping("/{candidaturaId}/validar-inhabilidades")
	public ResultadoValidacionInhabilidadesDto validarInhabilidades(@PathVariable Long candidaturaId,
			@RequestParam(required = false, defaultValue = "pendiente-configuracion") String endpointExterno) {
		return servicioCandidatura.validarInhabilidades(candidaturaId, endpointExterno);
	}

	@GetMapping("/elecciones/{eleccionId}")
	public List<CandidaturaRespuestaDto> listarPorEleccion(@PathVariable Long eleccionId) {
		return servicioCandidatura.listarPorEleccion(eleccionId);
	}

	@GetMapping("/elecciones/{eleccionId}/documentos")
	public List<String> listarDocumentosPorEleccion(@PathVariable Long eleccionId) {
		return servicioCandidatura.listarDocumentosPorEleccion(eleccionId);
	}

	@PostMapping("/elecciones/{eleccionId}/tarjeton")
	public TarjetonRespuestaDto generarTarjeton(@PathVariable Long eleccionId,
			@Valid @RequestBody GenerarTarjetonSolicitudDto solicitud) {
		return servicioCandidatura.generarTarjeton(eleccionId, solicitud);
	}

	@GetMapping("/elecciones/{eleccionId}/tarjeton")
	public TarjetonRespuestaDto obtenerUltimoTarjeton(@PathVariable Long eleccionId) {
		return servicioCandidatura.obtenerUltimoTarjeton(eleccionId);
	}

	@GetMapping("/{candidaturaId}/versiones")
	public List<CandidaturaVersion> listarVersiones(@PathVariable Long candidaturaId) {
		return servicioCandidatura.listarVersiones(candidaturaId);
	}

	@PostMapping("/{candidaturaId}/foto")
	public ResponseEntity<CandidaturaRespuestaDto> subirFoto(@PathVariable Long candidaturaId,
			@RequestParam("archivo") MultipartFile archivo) {
		String nombreArchivo = servicioAlmacenamientoFotos.almacenarFoto(candidaturaId, archivo);
		String fotoUrl = "/api/candidaturas/fotos/" + nombreArchivo;
		CandidaturaRespuestaDto respuesta = servicioCandidatura.actualizarFotoUrl(candidaturaId, fotoUrl);
		return ResponseEntity.created(URI.create(fotoUrl)).body(respuesta);
	}

	@GetMapping("/fotos/{filename:.+}")
	public ResponseEntity<Resource> servirFoto(@PathVariable String filename) {
		Resource recurso = servicioAlmacenamientoFotos.cargarFoto(filename);
		String tipoContenido = servicioAlmacenamientoFotos.determinarTipoContenido(filename);
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType(tipoContenido))
				.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
				.body(recurso);
	}
}