package com.selloLegitimo.GestionPreElectoral.modelo;

import java.time.LocalDateTime;

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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "candidaturas", uniqueConstraints = {
	@UniqueConstraint(name = "uk_candidatura_eleccion_documento", columnNames = { "eleccion_id", "documento" })
})
public class Candidatura {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "eleccion_id", nullable = false)
	private Long eleccionId;

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

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "candidatura_reemplazada_id")
	private Candidatura candidaturaReemplazada;

	@Enumerated(EnumType.STRING)
	@Column(name = "motivo_reemplazo", length = 20)
	private MotivoReemplazoCandidatura motivoReemplazo;

	@Column(name = "justificacion_reemplazo", length = 500)
	private String justificacionReemplazo;

	@Column(name = "actor_ultima_modificacion", nullable = false, length = 120)
	private String actorUltimaModificacion;

	@Column(name = "fecha_inscripcion", nullable = false)
	private LocalDateTime fechaInscripcion;

	@Column(name = "fecha_actualizacion", nullable = false)
	private LocalDateTime fechaActualizacion;

	public Candidatura() {
	}

	public Candidatura(Long eleccionId, String nombreCandidato, String documento, String partido,
			String circunscripcion, String fotoUrl, EstadoCandidatura estado, String actorUltimaModificacion) {
		this.eleccionId = eleccionId;
		this.nombreCandidato = nombreCandidato;
		this.documento = documento;
		this.partido = partido;
		this.circunscripcion = circunscripcion;
		this.fotoUrl = fotoUrl;
		this.estado = estado;
		this.actorUltimaModificacion = actorUltimaModificacion;
	}

	@PrePersist
	public void prePersist() {
		LocalDateTime ahora = LocalDateTime.now();
		this.fechaInscripcion = ahora;
		this.fechaActualizacion = ahora;
	}

	@PreUpdate
	public void preUpdate() {
		this.fechaActualizacion = LocalDateTime.now();
	}

	public void actualizarDatos(String nombreCandidato, String partido, String circunscripcion, String fotoUrl,
			String actor) {
		this.nombreCandidato = nombreCandidato;
		this.partido = partido;
		this.circunscripcion = circunscripcion;
		this.fotoUrl = fotoUrl;
		this.actorUltimaModificacion = actor;
	}

	public void actualizarEstado(EstadoCandidatura estado, String actor) {
		this.estado = estado;
		this.actorUltimaModificacion = actor;
	}

	public void marcarComoReemplazada(Candidatura nuevaCandidatura, MotivoReemplazoCandidatura motivoReemplazo,
			String justificacionReemplazo, String actor) {
		this.estado = EstadoCandidatura.REEMPLAZADA;
		this.actorUltimaModificacion = actor;
		this.motivoReemplazo = motivoReemplazo;
		this.justificacionReemplazo = justificacionReemplazo;
	}

	public void definirOrigenReemplazo(Candidatura candidaturaReemplazada, MotivoReemplazoCandidatura motivoReemplazo,
			String justificacionReemplazo, String actor) {
		this.candidaturaReemplazada = candidaturaReemplazada;
		this.motivoReemplazo = motivoReemplazo;
		this.justificacionReemplazo = justificacionReemplazo;
		this.actorUltimaModificacion = actor;
	}

	public Long getId() {
		return id;
	}

	public Long getEleccionId() {
		return eleccionId;
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

	public Candidatura getCandidaturaReemplazada() {
		return candidaturaReemplazada;
	}

	public MotivoReemplazoCandidatura getMotivoReemplazo() {
		return motivoReemplazo;
	}

	public String getJustificacionReemplazo() {
		return justificacionReemplazo;
	}

	public String getActorUltimaModificacion() {
		return actorUltimaModificacion;
	}

	public LocalDateTime getFechaInscripcion() {
		return fechaInscripcion;
	}

	public LocalDateTime getFechaActualizacion() {
		return fechaActualizacion;
	}
}