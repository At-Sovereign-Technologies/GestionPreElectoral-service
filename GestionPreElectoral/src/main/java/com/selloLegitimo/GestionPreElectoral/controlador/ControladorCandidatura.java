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

import com.selloLegitimo.GestionPreElectoral.dto.ActualizarDatosCandidaturaSolicitudDto;
import com.selloLegitimo.GestionPreElectoral.dto.ActualizarEstadoCandidaturaSolicitudDto;
import com.selloLegitimo.GestionPreElectoral.dto.CandidaturaRespuestaDto;
import com.selloLegitimo.GestionPreElectoral.dto.RegistrarCandidaturaSolicitudDto;
import com.selloLegitimo.GestionPreElectoral.dto.ReemplazarCandidaturaSolicitudDto;
import com.selloLegitimo.GestionPreElectoral.dto.ResultadoValidacionInhabilidadesDto;
import com.selloLegitimo.GestionPreElectoral.servicio.ServicioCandidatura;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/candidaturas")
public class ControladorCandidatura {

	@Autowired
	private ServicioCandidatura servicioCandidatura;

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
}