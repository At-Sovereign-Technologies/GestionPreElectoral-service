package com.selloLegitimo.GestionPreElectoral.dto;

public class ResumenCensoDto {

	private long total;
	private long habilitados;
	private long excluidos;
	private long exentos;

	public ResumenCensoDto(long total, long habilitados, long excluidos, long exentos) {
		this.total = total;
		this.habilitados = habilitados;
		this.excluidos = excluidos;
		this.exentos = exentos;
	}

	public long getTotal() {
		return total;
	}

	public long getHabilitados() {
		return habilitados;
	}

	public long getExcluidos() {
		return excluidos;
	}

	public long getExentos() {
		return exentos;
	}
}
