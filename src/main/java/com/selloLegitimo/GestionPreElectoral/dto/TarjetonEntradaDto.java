package com.selloLegitimo.GestionPreElectoral.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TarjetonEntradaDto {
	private int orden;
	private String nombreCandidato;
	private String partido;
	private String fotoUrl;
	private TipoEntradaTarjeton tipo;

	public enum TipoEntradaTarjeton {
		CANDIDATO,
		VOTO_BLANCO
	}
}
