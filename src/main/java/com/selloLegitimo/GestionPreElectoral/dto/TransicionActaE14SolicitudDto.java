package com.selloLegitimo.GestionPreElectoral.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.selloLegitimo.GestionPreElectoral.modelo.EstadoActaE14;

public class TransicionActaE14SolicitudDto {

	@NotNull
	private UUID actaUuid;

	@NotNull
	private EstadoActaE14 nuevoEstado;

	@NotBlank
	private String actorId;

	private String deviceId;

	private byte[] documentBytes;

	private String authorizationRef;

	private String metadataJson;

	public UUID getActaUuid() {
		return actaUuid;
	}

	public void setActaUuid(UUID actaUuid) {
		this.actaUuid = actaUuid;
	}

	public EstadoActaE14 getNuevoEstado() {
		return nuevoEstado;
	}

	public String getActorId() {
		return actorId;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public byte[] getDocumentBytes() {
		return documentBytes;
	}

	public String getAuthorizationRef() {
		return authorizationRef;
	}

	public String getMetadataJson() {
		return metadataJson;
	}
}
