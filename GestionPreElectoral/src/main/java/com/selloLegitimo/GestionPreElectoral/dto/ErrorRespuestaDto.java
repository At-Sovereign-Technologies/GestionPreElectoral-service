package com.selloLegitimo.GestionPreElectoral.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorRespuestaDto {
	private LocalDateTime fecha;
	private int estado;
	private String error;
}