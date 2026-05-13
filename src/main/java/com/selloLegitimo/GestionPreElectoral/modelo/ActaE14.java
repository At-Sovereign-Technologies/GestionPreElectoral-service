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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "acta_e14", schema = "gestion_pre_electoral")
public class ActaE14 {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", nullable = false)
	private UUID uuid;

	@Column(name = "mesa_id", nullable = false, length = 100)
	private String mesaId;

	@Column(name = "eleccion_id", nullable = false)
	private Long eleccionId;

	@Column(name = "numero_formulario", length = 50)
	private String numeroFormulario;

	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 50)
	private EstadoActaE14 estado;

	public ActaE14() {
	}

	@PrePersist
	public void prePersist() {
		if (this.uuid == null) {
			this.uuid = UUID.randomUUID();
		}
		if (this.createdAt == null) {
			this.createdAt = OffsetDateTime.now();
		}
		if (this.updatedAt == null) {
			this.updatedAt = OffsetDateTime.now();
		}
		if (this.estado == null) {
			this.estado = EstadoActaE14.CREADA;
		}
	}

	@PreUpdate
	public void preUpdate() {
		this.updatedAt = OffsetDateTime.now();
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public String getMesaId() {
		return mesaId;
	}

	public void setMesaId(String mesaId) {
		this.mesaId = mesaId;
	}

	public Long getEleccionId() {
		return eleccionId;
	}

	public void setEleccionId(Long eleccionId) {
		this.eleccionId = eleccionId;
	}

	public String getNumeroFormulario() {
		return numeroFormulario;
	}

	public void setNumeroFormulario(String numeroFormulario) {
		this.numeroFormulario = numeroFormulario;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(OffsetDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public OffsetDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(OffsetDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public EstadoActaE14 getEstado() {
		return estado;
	}

	public void setEstado(EstadoActaE14 estado) {
		this.estado = estado;
	}
}
