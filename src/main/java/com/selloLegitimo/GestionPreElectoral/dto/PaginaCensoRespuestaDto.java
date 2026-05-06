package com.selloLegitimo.GestionPreElectoral.dto;

import java.util.List;

public class PaginaCensoRespuestaDto {

	private List<RegistroCensoRespuestaDto> contenido;
	private long totalElementos;
	private int totalPaginas;
	private int numeroPagina;
	private int tamanoPagina;

	public PaginaCensoRespuestaDto(List<RegistroCensoRespuestaDto> contenido, long totalElementos, int totalPaginas,
			int numeroPagina, int tamanoPagina) {
		this.contenido = contenido;
		this.totalElementos = totalElementos;
		this.totalPaginas = totalPaginas;
		this.numeroPagina = numeroPagina;
		this.tamanoPagina = tamanoPagina;
	}

	public List<RegistroCensoRespuestaDto> getContenido() {
		return contenido;
	}

	public long getTotalElementos() {
		return totalElementos;
	}

	public int getTotalPaginas() {
		return totalPaginas;
	}

	public int getNumeroPagina() {
		return numeroPagina;
	}

	public int getTamanoPagina() {
		return tamanoPagina;
	}
}
