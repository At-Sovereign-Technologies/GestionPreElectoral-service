package com.selloLegitimo.GestionPreElectoral.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CongelamientoCensoRespuestaDto {
	private Long eleccionId;
	private String estado;
	private String hashRaiz;
	private int totalRegistros;
}