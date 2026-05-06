package com.selloLegitimo.GestionPreElectoral.dto;

import com.selloLegitimo.GestionPreElectoral.modelo.TipoOrdenamiento;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerarTarjetonSolicitudDto {
	private String circunscripcion;

	@NotNull
	private TipoOrdenamiento tipoOrdenamiento;

	private Long semillaAleatoria;

	@NotBlank
	private String actor;
}
