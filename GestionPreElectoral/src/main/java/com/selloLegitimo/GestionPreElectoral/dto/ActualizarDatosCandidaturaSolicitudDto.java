package com.selloLegitimo.GestionPreElectoral.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActualizarDatosCandidaturaSolicitudDto {
	@NotBlank
	private String nombreCandidato;

	@NotBlank
	private String partido;

	@NotBlank
	private String circunscripcion;

	private String fotoUrl;

	@NotBlank
	private String actor;
}