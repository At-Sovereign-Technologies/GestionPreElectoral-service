package com.selloLegitimo.GestionPreElectoral.dto;

import com.selloLegitimo.GestionPreElectoral.modelo.EstadoCandidatura;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActualizarEstadoCandidaturaSolicitudDto {
	@NotNull
	private EstadoCandidatura estado;

	@NotBlank
	private String actor;
}