package com.selloLegitimo.GestionPreElectoral.modelo;

import java.time.LocalDateTime;

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
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "registros_censo", uniqueConstraints = {
	@UniqueConstraint(name = "uk_registro_censo", columnNames = { "eleccion_id", "ciudadano_id" })
})
public class RegistroCenso {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "eleccion_id", nullable = false)
	private Long eleccionId;

	@jakarta.persistence.ManyToOne(fetch = jakarta.persistence.FetchType.LAZY, optional = false)
	@jakarta.persistence.JoinColumn(name = "ciudadano_id", nullable = false)
	private Ciudadano ciudadano;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private EstadoCenso estado;

	@Enumerated(EnumType.STRING)
	@Column(name = "causal_estado", length = 50)
	private CausalCenso causalEstado;

	@Column(length = 500)
	private String observacion;

	@Column(name = "actor_ultima_modificacion", nullable = false, length = 120)
	private String actorUltimaModificacion;

	@Column(name = "fecha_creacion", nullable = false)
	private LocalDateTime fechaCreacion;

	@Column(name = "fecha_actualizacion", nullable = false)
	private LocalDateTime fechaActualizacion;

	public RegistroCenso() {
	}

	public RegistroCenso(Long eleccionId, Ciudadano ciudadano, EstadoCenso estado, CausalCenso causalEstado,
			String observacion, String actorUltimaModificacion) {
		this.eleccionId = eleccionId;
		this.ciudadano = ciudadano;
		this.estado = estado;
		this.causalEstado = causalEstado;
		this.observacion = observacion;
		this.actorUltimaModificacion = actorUltimaModificacion;
	}

	@PrePersist
	public void prePersist() {
		LocalDateTime ahora = LocalDateTime.now();
		this.fechaCreacion = ahora;
		this.fechaActualizacion = ahora;
	}

	@PreUpdate
	public void preUpdate() {
		this.fechaActualizacion = LocalDateTime.now();
	}

	public void actualizarEstado(EstadoCenso estado, CausalCenso causalEstado, String observacion, String actor) {
		this.estado = estado;
		this.causalEstado = causalEstado;
		this.observacion = observacion;
		this.actorUltimaModificacion = actor;
	}

	public Long getId() {
		return id;
	}

	public Long getEleccionId() {
		return eleccionId;
	}

	public Ciudadano getCiudadano() {
		return ciudadano;
	}

	public EstadoCenso getEstado() {
		return estado;
	}

	public CausalCenso getCausalEstado() {
		return causalEstado;
	}

	public String getObservacion() {
		return observacion;
	}

	public String getActorUltimaModificacion() {
		return actorUltimaModificacion;
	}

	public LocalDateTime getFechaActualizacion() {
		return fechaActualizacion;
	}
}