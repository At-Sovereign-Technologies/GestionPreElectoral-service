package com.selloLegitimo.GestionPreElectoral.controlador;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.selloLegitimo.GestionPreElectoral.dto.EleccionResumenDto;
import com.selloLegitimo.GestionPreElectoral.servicio.ServicioEleccion;

@RestController
@RequestMapping({ "/api/v1/elections", "/api/v1/elecciones" })
public class ControladorEleccion {

	private final ServicioEleccion servicioEleccion;

	public ControladorEleccion(ServicioEleccion servicioEleccion) {
		this.servicioEleccion = servicioEleccion;
	}

	@GetMapping
	public List<EleccionResumenDto> listarElecciones() {
		return servicioEleccion.listarElecciones();
	}

	@GetMapping("/active")
	public List<EleccionResumenDto> listarEleccionesActivas() {
		return servicioEleccion.listarElecciones().stream()
				.filter(e -> "ABIERTA".equalsIgnoreCase(e.estado()))
				.toList();
	}
}
