package com.selloLegitimo.GestionPreElectoral.modelo;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "acta_e14_lifecycle", schema = "gestion_pre_electoral")
public class ActaE14Lifecycle {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", nullable = false)
	private UUID uuid;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "acta_id", nullable = false)
	private ActaE14 acta;

	@Column(name = "version_number", nullable = false)
	private Integer versionNumber;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "previous_version_id")
	private ActaE14Lifecycle previousVersion;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 50)
	private EstadoActaE14 estado;

	@Column(name = "timestamp_ntp", nullable = false)
	private OffsetDateTime timestampNtp;

	@Column(name = "actor_id", nullable = false, length = 120)
	private String actorId;

	@Column(name = "device_id", length = 255)
	private String deviceId;

	@Column(name = "document_sha256", length = 64)
	private String documentSha256;

	@Column(name = "authorization_ref", length = 255)
	private String authorizationRef;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb")
	private String metadata;

	public ActaE14Lifecycle() {
	}

	@PrePersist
	public void prePersist() {
		if (this.uuid == null) {
			this.uuid = UUID.randomUUID();
		}
		if (this.timestampNtp == null) {
			this.timestampNtp = OffsetDateTime.now();
		}
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public ActaE14 getActa() {
		return acta;
	}

	public void setActa(ActaE14 acta) {
		this.acta = acta;
	}

	public Integer getVersionNumber() {
		return versionNumber;
	}

	public void setVersionNumber(Integer versionNumber) {
		this.versionNumber = versionNumber;
	}

	public ActaE14Lifecycle getPreviousVersion() {
		return previousVersion;
	}

	public void setPreviousVersion(ActaE14Lifecycle previousVersion) {
		this.previousVersion = previousVersion;
	}

	public EstadoActaE14 getEstado() {
		return estado;
	}

	public void setEstado(EstadoActaE14 estado) {
		this.estado = estado;
	}

	public OffsetDateTime getTimestampNtp() {
		return timestampNtp;
	}

	public void setTimestampNtp(OffsetDateTime timestampNtp) {
		this.timestampNtp = timestampNtp;
	}

	public String getActorId() {
		return actorId;
	}

	public void setActorId(String actorId) {
		this.actorId = actorId;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getDocumentSha256() {
		return documentSha256;
	}

	public void setDocumentSha256(String documentSha256) {
		this.documentSha256 = documentSha256;
	}

	public String getAuthorizationRef() {
		return authorizationRef;
	}

	public void setAuthorizationRef(String authorizationRef) {
		this.authorizationRef = authorizationRef;
	}

	public String getMetadata() {
		return metadata;
	}

	public void setMetadata(String metadata) {
		this.metadata = metadata;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private final ActaE14Lifecycle l = new ActaE14Lifecycle();

		public Builder acta(ActaE14 acta) {
			l.acta = acta;
			return this;
		}

		public Builder versionNumber(Integer versionNumber) {
			l.versionNumber = versionNumber;
			return this;
		}

		public Builder previousVersion(ActaE14Lifecycle previousVersion) {
			l.previousVersion = previousVersion;
			return this;
		}

		public Builder estado(EstadoActaE14 estado) {
			l.estado = estado;
			return this;
		}

		public Builder timestampNtp(OffsetDateTime timestampNtp) {
			l.timestampNtp = timestampNtp;
			return this;
		}

		public Builder actorId(String actorId) {
			l.actorId = actorId;
			return this;
		}

		public Builder deviceId(String deviceId) {
			l.deviceId = deviceId;
			return this;
		}

		public Builder documentSha256(String documentSha256) {
			l.documentSha256 = documentSha256;
			return this;
		}

		public Builder authorizationRef(String authorizationRef) {
			l.authorizationRef = authorizationRef;
			return this;
		}

		public Builder metadata(String metadata) {
			l.metadata = metadata;
			return this;
		}

		public ActaE14Lifecycle build() {
			return l;
		}
	}
}
