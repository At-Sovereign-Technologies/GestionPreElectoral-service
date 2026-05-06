package com.selloLegitimo.GestionPreElectoral.modelo;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "candidatura_versiones", schema = "gestion_pre_electoral")
public class CandidaturaVersion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "candidatura_id", nullable = false)
	private Candidatura candidatura;

	@Column(name = "version_number", nullable = false)
	private Long versionNumber;

	@Column(name = "nombre_candidato", nullable = false, length = 180)
	private String nombreCandidato;

	@Column(nullable = false, length = 30)
	private String documento;

	@Column(nullable = false, length = 120)
	private String partido;

	@Column(nullable = false, length = 120)
	private String circunscripcion;

	@Column(name = "foto_url", length = 500)
	private String fotoUrl;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private EstadoCandidatura estado;

	@Column(name = "actor_modificacion", nullable = false, length = 120)
	private String actorModificacion;

	@Column(name = "fecha_version", nullable = false)
	private LocalDateTime fechaVersion;

	public CandidaturaVersion() {
	}

	public CandidaturaVersion(Candidatura candidatura, Long versionNumber, String actorModificacion) {
		this.candidatura = candidatura;
		this.versionNumber = versionNumber;
		this.nombreCandidato = candidatura.getNombreCandidato();
		this.documento = candidatura.getDocumento();
		this.partido = candidatura.getPartido();
		this.circunscripcion = candidatura.getCircunscripcion();
		this.fotoUrl = candidatura.getFotoUrl();
		this.estado = candidatura.getEstado();
		this.actorModificacion = actorModificacion;
	}

	@PrePersist
	public void prePersist() {
		this.fechaVersion = LocalDateTime.now();
	}

	public Long getId() {
		return id;
	}

	public Candidatura getCandidatura() {
		return candidatura;
	}

	public Long getVersionNumber() {
		return versionNumber;
	}

	public String getNombreCandidato() {
		return nombreCandidato;
	}

	public String getDocumento() {
		return documento;
	}

	public String getPartido() {
		return partido;
	}

	public String getCircunscripcion() {
		return circunscripcion;
	}

	public String getFotoUrl() {
		return fotoUrl;
	}

	public EstadoCandidatura getEstado() {
		return estado;
	}

	public String getActorModificacion() {
		return actorModificacion;
	}

	public LocalDateTime getFechaVersion() {
		return fechaVersion;
	}
}
