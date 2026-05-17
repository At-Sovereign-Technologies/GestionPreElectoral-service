package com.selloLegitimo.GestionPreElectoral.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public class RegistroAuditoriaRespuestaDto {

	private Long id;
	private UUID eventId;
	private String actorId;
	private String action;
	private String entityType;
	private String entityId;
	private OffsetDateTime timestampNtp;
	private String ipAddress;
	private String deviceId;
	private String payloadHash;
	private String chainHash;

	public RegistroAuditoriaRespuestaDto() {
	}

	public RegistroAuditoriaRespuestaDto(Long id, UUID eventId, String actorId, String action,
			String entityType, String entityId, OffsetDateTime timestampNtp,
			String ipAddress, String deviceId, String payloadHash, String chainHash) {
		this.id = id;
		this.eventId = eventId;
		this.actorId = actorId;
		this.action = action;
		this.entityType = entityType;
		this.entityId = entityId;
		this.timestampNtp = timestampNtp;
		this.ipAddress = ipAddress;
		this.deviceId = deviceId;
		this.payloadHash = payloadHash;
		this.chainHash = chainHash;
	}

	public Long getId() {
		return id;
	}

	public UUID getEventId() {
		return eventId;
	}

	public String getActorId() {
		return actorId;
	}

	public String getAction() {
		return action;
	}

	public String getEntityType() {
		return entityType;
	}

	public String getEntityId() {
		return entityId;
	}

	public OffsetDateTime getTimestampNtp() {
		return timestampNtp;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public String getPayloadHash() {
		return payloadHash;
	}

	public String getChainHash() {
		return chainHash;
	}
}
