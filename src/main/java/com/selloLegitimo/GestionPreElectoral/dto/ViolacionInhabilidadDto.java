package com.selloLegitimo.GestionPreElectoral.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ViolacionInhabilidadDto {
	private String codigoRegla;
	private String descripcion;
	private Severidad severidad;

	public enum Severidad {
		BLOQUEANTE,
		ADVERTENCIA
	}
}
