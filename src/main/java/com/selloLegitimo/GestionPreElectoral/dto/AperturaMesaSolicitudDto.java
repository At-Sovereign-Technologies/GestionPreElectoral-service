package com.selloLegitimo.GestionPreElectoral.dto;

import java.util.List;

public class AperturaMesaSolicitudDto {

	private List<String> tokens;

	public AperturaMesaSolicitudDto() {
	}

	public AperturaMesaSolicitudDto(List<String> tokens) {
		this.tokens = tokens;
	}

	public List<String> getTokens() {
		return tokens;
	}
}