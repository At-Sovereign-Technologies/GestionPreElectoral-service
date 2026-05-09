package com.selloLegitimo.GestionPreElectoral.controlador;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.selloLegitimo.GestionPreElectoral.dto.AperturaMesaRespuestaDto;
import com.selloLegitimo.GestionPreElectoral.dto.AperturaMesaSolicitudDto;
import com.selloLegitimo.GestionPreElectoral.dto.JuradoMesaRespuestaDto;
import com.selloLegitimo.GestionPreElectoral.dto.SincronizarJuradoMesaSolicitudDto;
import com.selloLegitimo.GestionPreElectoral.servicio.ServicioJuradoMesa;

@Validated
@RestController
@RequestMapping("/api/v1")
public class ControladorJuradosMesas {

	private final ServicioJuradoMesa servicioJuradoMesa;

	public ControladorJuradosMesas(ServicioJuradoMesa servicioJuradoMesa) {
		this.servicioJuradoMesa = servicioJuradoMesa;
	}

	@PostMapping("/jurados/sincronizar")
	public List<JuradoMesaRespuestaDto> sincronizar(@RequestBody List<SincronizarJuradoMesaSolicitudDto> solicitudes) {
		return servicioJuradoMesa.sincronizar(solicitudes);
	}

	@PostMapping("/mesas/{mesaId}/apertura")
	public AperturaMesaRespuestaDto abrirMesa(@PathVariable Long mesaId, @RequestBody AperturaMesaSolicitudDto solicitud) {
		return servicioJuradoMesa.abrirMesa(mesaId, solicitud);
	}
}