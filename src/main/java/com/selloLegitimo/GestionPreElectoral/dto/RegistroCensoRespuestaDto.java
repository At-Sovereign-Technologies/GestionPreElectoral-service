package com.selloLegitimo.GestionPreElectoral.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.selloLegitimo.GestionPreElectoral.modelo.CausalCenso;
import com.selloLegitimo.GestionPreElectoral.modelo.EstadoCenso;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistroCensoRespuestaDto {
	private Long id;
	private Long eleccionId;
	private String tipoDocumento;
	private String numeroDocumento;
	private String nombres;
	private String apellidos;
	private LocalDate fechaNacimiento;
	private String departamento;
	private String municipio;
	private EstadoCenso estado;
	private CausalCenso causalEstado;
	private String observacion;
	private String actorUltimaModificacion;
	private LocalDateTime fechaActualizacion;
}