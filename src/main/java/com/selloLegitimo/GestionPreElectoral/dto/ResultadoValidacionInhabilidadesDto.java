package com.selloLegitimo.GestionPreElectoral.dto;

import java.util.ArrayList;
import java.util.List;

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
	private boolean valido;
	private List<ViolacionInhabilidadDto> violaciones = new ArrayList<>();

	public ResultadoValidacionInhabilidadesDto(Long candidaturaId, String mensaje, String endpointExterno) {
		this.candidaturaId = candidaturaId;
		this.mensaje = mensaje;
		this.endpointExterno = endpointExterno;
		this.valido = true;
	}
}