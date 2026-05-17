package com.selloLegitimo.GestionPreElectoral.dto;

import java.util.UUID;

public class ActaE14RespuestaDto {

	private UUID uuid;
	private String mesaId;
	private Long eleccionId;
	private String numeroFormulario;
	private String estado;
	private String createdAt;
	private String updatedAt;

	public ActaE14RespuestaDto() {
	}

	public ActaE14RespuestaDto(UUID uuid, String mesaId, Long eleccionId,
			String numeroFormulario, String estado, String createdAt, String updatedAt) {
		this.uuid = uuid;
		this.mesaId = mesaId;
		this.eleccionId = eleccionId;
		this.numeroFormulario = numeroFormulario;
		this.estado = estado;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public UUID getUuid() {
		return uuid;
	}

	public String getMesaId() {
		return mesaId;
	}

	public Long getEleccionId() {
		return eleccionId;
	}

	public String getNumeroFormulario() {
		return numeroFormulario;
	}

	public String getEstado() {
		return estado;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public String getUpdatedAt() {
		return updatedAt;
	}
}
