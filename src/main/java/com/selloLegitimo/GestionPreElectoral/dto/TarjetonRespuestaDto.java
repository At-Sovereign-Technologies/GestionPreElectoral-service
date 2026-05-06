package com.selloLegitimo.GestionPreElectoral.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TarjetonRespuestaDto {
	private Long eleccionId;
	private String circunscripcion;
	private LocalDateTime fechaGeneracion;
	private Long semillaUsada;
	private List<TarjetonEntradaDto> entradas;
}
