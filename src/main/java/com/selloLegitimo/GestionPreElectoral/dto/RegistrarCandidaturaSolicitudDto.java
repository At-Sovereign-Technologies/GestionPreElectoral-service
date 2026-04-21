package com.selloLegitimo.GestionPreElectoral.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrarCandidaturaSolicitudDto {
	@NotNull
	private Long eleccionId;

	@NotBlank
	private String nombreCandidato;

	@NotBlank
	private String documento;

	@NotBlank
	private String partido;

	@NotBlank
	private String circunscripcion;

	private String fotoUrl;

	@NotBlank
	private String actor;
}