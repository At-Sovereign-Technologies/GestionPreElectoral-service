package com.selloLegitimo.GestionPreElectoral.dto;

public class VerificacionCadenaAuditoriaRespuestaDto {

	private boolean valida;
	private int totalRegistros;
	private int registrosVerificados;
	private Long primerRegistroFallidoId;
	private String mensajeError;

	public VerificacionCadenaAuditoriaRespuestaDto() {
	}

	public VerificacionCadenaAuditoriaRespuestaDto(boolean valida, int totalRegistros, int registrosVerificados,
			Long primerRegistroFallidoId, String mensajeError) {
		this.valida = valida;
		this.totalRegistros = totalRegistros;
		this.registrosVerificados = registrosVerificados;
		this.primerRegistroFallidoId = primerRegistroFallidoId;
		this.mensajeError = mensajeError;
	}

	public boolean isValida() {
		return valida;
	}

	public int getTotalRegistros() {
		return totalRegistros;
	}

	public int getRegistrosVerificados() {
		return registrosVerificados;
	}

	public Long getPrimerRegistroFallidoId() {
		return primerRegistroFallidoId;
	}

	public String getMensajeError() {
		return mensajeError;
	}
}
