package com.selloLegitimo.GestionPreElectoral.dto;

public class SincronizarJuradoMesaSolicitudDto {

	private String nombre;
	private String documento;
	private Long mesaId;
	private String rol;

	public SincronizarJuradoMesaSolicitudDto() {
	}

	public SincronizarJuradoMesaSolicitudDto(String nombre, String documento, Long mesaId, String rol) {
		this.nombre = nombre;
		this.documento = documento;
		this.mesaId = mesaId;
		this.rol = rol;
	}

	public String getNombre() {
		return nombre;
	}

	public String getDocumento() {
		return documento;
	}

	public Long getMesaId() {
		return mesaId;
	}

	public String getRol() {
		return rol;
	}
}