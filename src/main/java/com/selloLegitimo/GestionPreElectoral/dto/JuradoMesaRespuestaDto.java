package com.selloLegitimo.GestionPreElectoral.dto;

public class JuradoMesaRespuestaDto {

	private Long id;
	private String nombre;
	private String documento;
	private Long mesaId;
	private String rol;
	private String tokenAcceso;

	public JuradoMesaRespuestaDto() {
	}

	public JuradoMesaRespuestaDto(Long id, String nombre, String documento, Long mesaId, String rol, String tokenAcceso) {
		this.id = id;
		this.nombre = nombre;
		this.documento = documento;
		this.mesaId = mesaId;
		this.rol = rol;
		this.tokenAcceso = tokenAcceso;
	}

	public Long getId() {
		return id;
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

	public String getTokenAcceso() {
		return tokenAcceso;
	}
}