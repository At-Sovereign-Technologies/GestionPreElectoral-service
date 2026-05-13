package com.selloLegitimo.GestionPreElectoral.dto;

import java.util.List;

public class AperturaMesaRespuestaDto {

	private String estado;
	private List<String> formularios;

	public AperturaMesaRespuestaDto() {
	}

	public AperturaMesaRespuestaDto(String estado, List<String> formularios) {
		this.estado = estado;
		this.formularios = formularios;
	}

	public String getEstado() {
		return estado;
	}

	public List<String> getFormularios() {
		return formularios;
	}
}