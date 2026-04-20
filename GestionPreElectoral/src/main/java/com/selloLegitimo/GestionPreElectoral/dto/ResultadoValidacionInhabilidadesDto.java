package com.selloLegitimo.GestionPreElectoral.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultadoValidacionInhabilidadesDto {
	private Long candidaturaId;
	private String mensaje;
	private String endpointExterno;
}