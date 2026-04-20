package com.selloLegitimo.GestionPreElectoral.dto;

import com.selloLegitimo.GestionPreElectoral.modelo.CausalCenso;
import com.selloLegitimo.GestionPreElectoral.modelo.EstadoCenso;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActualizarEstadoCensoSolicitudDto {
	@NotNull
	private EstadoCenso estado;

	private CausalCenso causalEstado;

	private String observacion;

	@NotBlank
	private String actor;

	private AutorizacionCierreSolicitudDto autorizacionCierre;
}