package com.selloLegitimo.GestionPreElectoral.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.selloLegitimo.GestionPreElectoral.modelo.TipoAccionAuditoria;

public class RegistroAuditoriaSolicitudDto {

	@NotBlank
	private String actorId;

	@NotNull
	private TipoAccionAuditoria action;

	@NotBlank
	private String entityType;

	@NotBlank
	private String entityId;

	private String ipAddress;

	private String deviceId;

	public String getActorId() {
		return actorId;
	}

	public TipoAccionAuditoria getAction() {
		return action;
	}

	public String getEntityType() {
		return entityType;
	}

	public String getEntityId() {
		return entityId;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public String getDeviceId() {
		return deviceId;
	}
}
