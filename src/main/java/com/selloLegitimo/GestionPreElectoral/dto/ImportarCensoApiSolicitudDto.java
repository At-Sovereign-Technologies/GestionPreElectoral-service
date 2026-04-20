package com.selloLegitimo.GestionPreElectoral.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportarCensoApiSolicitudDto {
	@NotNull
	private Long eleccionId;

	@NotBlank
	private String url;

	@NotBlank
	private String actor;

	private AutorizacionCierreSolicitudDto autorizacionCierre;
}