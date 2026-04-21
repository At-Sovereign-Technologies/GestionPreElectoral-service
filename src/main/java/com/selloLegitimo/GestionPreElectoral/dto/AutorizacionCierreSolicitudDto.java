package com.selloLegitimo.GestionPreElectoral.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AutorizacionCierreSolicitudDto {
	private String superadministrador;
	private String justificacion;
}