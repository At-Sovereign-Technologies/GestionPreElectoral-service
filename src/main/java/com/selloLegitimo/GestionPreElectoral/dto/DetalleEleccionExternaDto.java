package com.selloLegitimo.GestionPreElectoral.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.selloLegitimo.GestionPreElectoral.modelo.EstadoEleccion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetalleEleccionExternaDto {
	private Long id;
	private String nombre;
	private EstadoEleccion estado;
	private LocalDateTime fechaApertura;
	private LocalDateTime fechaCierre;
	private String documentoNoVotable;
	private LocalDateTime fechaInicioModificacionCandidaturas;
	private LocalDateTime fechaFinModificacionCandidaturas;
	private LocalDateTime fechaLimiteReemplazoCandidaturas;
	private List<String> excencionesHabilitadas;
	private Integer edadMinimaCandidatura;

	public boolean estaCerrada() {
		return EstadoEleccion.CERRADA.equals(this.estado)
				|| (this.fechaCierre != null && LocalDateTime.now().isAfter(this.fechaCierre));
	}

	public boolean estaDentroPeriodoModificacionCandidaturas() {
		if (this.fechaInicioModificacionCandidaturas == null || this.fechaFinModificacionCandidaturas == null) {
			return false;
		}
		LocalDateTime ahora = LocalDateTime.now();
		return !ahora.isBefore(this.fechaInicioModificacionCandidaturas)
				&& !ahora.isAfter(this.fechaFinModificacionCandidaturas);
	}

	public boolean permiteReemplazoCandidaturas() {
		return this.fechaLimiteReemplazoCandidaturas != null
				&& !LocalDateTime.now().isAfter(this.fechaLimiteReemplazoCandidaturas);
	}
}