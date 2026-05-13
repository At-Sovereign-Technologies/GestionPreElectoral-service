package com.selloLegitimo.GestionPreElectoral.dto;

import java.util.UUID;

public class ActaE14LifecycleRespuestaDto {

	private UUID uuid;
	private UUID actaUuid;
	private Integer versionNumber;
	private UUID previousVersionId;
	private String estado;
	private String timestampNtp;
	private String actorId;
	private String deviceId;
	private String documentSha256;
	private String authorizationRef;
	private String metadata;

	public ActaE14LifecycleRespuestaDto() {
	}

	public ActaE14LifecycleRespuestaDto(UUID uuid, UUID actaUuid, Integer versionNumber,
			UUID previousVersionId, String estado, String timestampNtp, String actorId,
			String deviceId, String documentSha256, String authorizationRef, String metadata) {
		this.uuid = uuid;
		this.actaUuid = actaUuid;
		this.versionNumber = versionNumber;
		this.previousVersionId = previousVersionId;
		this.estado = estado;
		this.timestampNtp = timestampNtp;
		this.actorId = actorId;
		this.deviceId = deviceId;
		this.documentSha256 = documentSha256;
		this.authorizationRef = authorizationRef;
		this.metadata = metadata;
	}

	public UUID getUuid() {
		return uuid;
	}

	public UUID getActaUuid() {
		return actaUuid;
	}

	public Integer getVersionNumber() {
		return versionNumber;
	}

	public UUID getPreviousVersionId() {
		return previousVersionId;
	}

	public String getEstado() {
		return estado;
	}

	public String getTimestampNtp() {
		return timestampNtp;
	}

	public String getActorId() {
		return actorId;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public String getDocumentSha256() {
		return documentSha256;
	}

	public String getAuthorizationRef() {
		return authorizationRef;
	}

	public String getMetadata() {
		return metadata;
	}
}
