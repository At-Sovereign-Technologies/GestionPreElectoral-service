package com.selloLegitimo.GestionPreElectoral.controlador;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.selloLegitimo.GestionPreElectoral.dto.ActaE14LifecycleRespuestaDto;
import com.selloLegitimo.GestionPreElectoral.dto.ActaE14RespuestaDto;
import com.selloLegitimo.GestionPreElectoral.dto.TransicionActaE14SolicitudDto;
import com.selloLegitimo.GestionPreElectoral.dto.VerificacionActaE14RespuestaDto;
import com.selloLegitimo.GestionPreElectoral.servicio.ServicioCicloVidaActa;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/actas")
public class ControladorActaE14 {

	@Autowired
	private ServicioCicloVidaActa servicioCicloVidaActa;

	@PostMapping
	public ResponseEntity<ActaE14RespuestaDto> crear(
			@RequestParam String mesaId,
			@RequestParam Long eleccionId,
			@RequestParam String actorId,
			@RequestParam(required = false) String deviceId) {

		ActaE14RespuestaDto acta = servicioCicloVidaActa.crearActa(mesaId, eleccionId, actorId, deviceId);
		return ResponseEntity.status(HttpStatus.CREATED).body(acta);
	}

	@PostMapping("/{uuid}/transicion")
	public ActaE14LifecycleRespuestaDto transicionar(
			@PathVariable UUID uuid,
			@Valid @RequestBody TransicionActaE14SolicitudDto solicitud) {

		solicitud.setActaUuid(uuid);
		return servicioCicloVidaActa.recordTransition(solicitud);
	}

	@GetMapping("/{uuid}/timeline")
	public List<ActaE14LifecycleRespuestaDto> timeline(@PathVariable UUID uuid) {
		return servicioCicloVidaActa.getTimeline(uuid);
	}

	@GetMapping("/{uuid}/versions")
	public List<ActaE14LifecycleRespuestaDto> versions(@PathVariable UUID uuid) {
		return servicioCicloVidaActa.getVersionChain(uuid);
	}

	@GetMapping("/{uuid}/verificar")
	public VerificacionActaE14RespuestaDto verificar(@PathVariable UUID uuid) {
		return servicioCicloVidaActa.verifyIntegrity(uuid);
	}
}
