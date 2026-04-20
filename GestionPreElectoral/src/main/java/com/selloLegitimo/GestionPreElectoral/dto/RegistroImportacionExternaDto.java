package com.selloLegitimo.GestionPreElectoral.dto;

import java.time.LocalDate;

import com.selloLegitimo.GestionPreElectoral.modelo.CausalCenso;
import com.selloLegitimo.GestionPreElectoral.modelo.EstadoCenso;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistroImportacionExternaDto {
	private String tipoDocumento;
	private String numeroDocumento;
	private String nombres;
	private String apellidos;
	private LocalDate fechaNacimiento;
	private EstadoCenso estado;
	private CausalCenso causalEstado;
	private String observacion;
}