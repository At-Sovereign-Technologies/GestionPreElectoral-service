package com.selloLegitimo.GestionPreElectoral.dto;

import com.selloLegitimo.GestionPreElectoral.modelo.MotivoReemplazoCandidatura;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReemplazarCandidaturaSolicitudDto {
	@NotBlank
	private String nombreCandidato;

	@NotBlank
	private String documento;

	@NotBlank
	private String partido;

	@NotBlank
	private String circunscripcion;

	private String fotoUrl;

	@NotNull
	private MotivoReemplazoCandidatura motivoReemplazo;

	@NotBlank
	private String justificacionReemplazo;

	@NotBlank
	private String actor;
}