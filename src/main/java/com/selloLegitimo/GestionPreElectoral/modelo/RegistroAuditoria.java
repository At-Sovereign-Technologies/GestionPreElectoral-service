package com.selloLegitimo.GestionPreElectoral.modelo;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "audit_log", schema = "gestion_pre_electoral")
public class RegistroAuditoria {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "event_id", nullable = false)
	private UUID eventId;

	@Column(name = "actor_id", nullable = false, length = 120)
	private String actorId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 50)
	private TipoAccionAuditoria action;

	@Column(name = "entity_type", nullable = false, length = 100)
	private String entityType;

	@Column(name = "entity_id", nullable = false, length = 255)
	private String entityId;

	@Column(name = "timestamp_ntp", nullable = false)
	private OffsetDateTime timestampNtp;

	@Column(name = "ip_address", length = 45)
	private String ipAddress;

	@Column(name = "device_id", length = 255)
	private String deviceId;

	@Column(name = "payload_hash", nullable = false, length = 64)
	private String payloadHash;

	@Column(name = "previous_hash", length = 64)
	private String previousHash;

	@Column(name = "chain_hash", nullable = false, length = 64)
	private String chainHash;

	public RegistroAuditoria() {
	}

	public Long getId() {
		return id;
	}

	public UUID getEventId() {
		return eventId;
	}

	public void setEventId(UUID eventId) {
		this.eventId = eventId;
	}

	public String getActorId() {
		return actorId;
	}

	public String getAction() {
		return action != null ? action.name() : null;
	}

	public TipoAccionAuditoria getActionEnum() {
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

	public void setTimestampNtp(OffsetDateTime timestampNtp) {
		this.timestampNtp = timestampNtp;
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

	public void setPayloadHash(String payloadHash) {
		this.payloadHash = payloadHash;
	}

	public String getPreviousHash() {
		return previousHash;
	}

	public void setPreviousHash(String previousHash) {
		this.previousHash = previousHash;
	}

	public String getChainHash() {
		return chainHash;
	}

	public void setChainHash(String chainHash) {
		this.chainHash = chainHash;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private final RegistroAuditoria r = new RegistroAuditoria();

		public Builder eventId(UUID eventId) {
			r.eventId = eventId;
			return this;
		}

		public Builder actorId(String actorId) {
			r.actorId = actorId;
			return this;
		}

		public Builder action(TipoAccionAuditoria action) {
			r.action = action;
			return this;
		}

		public Builder entityType(String entityType) {
			r.entityType = entityType;
			return this;
		}

		public Builder entityId(String entityId) {
			r.entityId = entityId;
			return this;
		}

		public Builder timestampNtp(OffsetDateTime timestampNtp) {
			r.timestampNtp = timestampNtp;
			return this;
		}

		public Builder ipAddress(String ipAddress) {
			r.ipAddress = ipAddress;
			return this;
		}

		public Builder deviceId(String deviceId) {
			r.deviceId = deviceId;
			return this;
		}

		public Builder payloadHash(String payloadHash) {
			r.payloadHash = payloadHash;
			return this;
		}

		public Builder previousHash(String previousHash) {
			r.previousHash = previousHash;
			return this;
		}

		public Builder chainHash(String chainHash) {
			r.chainHash = chainHash;
			return this;
		}

		public RegistroAuditoria build() {
			return r;
		}
	}
}
