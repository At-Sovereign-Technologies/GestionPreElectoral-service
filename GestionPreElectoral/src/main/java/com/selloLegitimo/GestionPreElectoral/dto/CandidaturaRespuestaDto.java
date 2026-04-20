package com.selloLegitimo.GestionPreElectoral.dto;

import java.time.LocalDateTime;

import com.selloLegitimo.GestionPreElectoral.modelo.EstadoCandidatura;
import com.selloLegitimo.GestionPreElectoral.modelo.MotivoReemplazoCandidatura;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CandidaturaRespuestaDto {
	private Long id;
	private Long eleccionId;
	private String nombreCandidato;
	private String documento;
	private String partido;
	private String circunscripcion;
	private String fotoUrl;
	private EstadoCandidatura estado;
	private Long candidaturaReemplazadaId;
	private MotivoReemplazoCandidatura motivoReemplazo;
	private String justificacionReemplazo;
	private String actorUltimaModificacion;
	private LocalDateTime fechaInscripcion;
	private LocalDateTime fechaActualizacion;
}