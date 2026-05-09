package com.selloLegitimo.GestionPreElectoral.modelo;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "estado_congelamiento_censo", schema = "gestion_pre_electoral")
public class EstadoCongelamientoCenso {

	@Id
	@Column(name = "eleccion_id")
	private Long eleccionId;

	@Column(name = "estado_eleccion", nullable = false, length = 20)
	private String estadoEleccion;

	@Column(name = "censo_congelado", nullable = false)
	private boolean censoCongelado;

	@Column(name = "hash_raiz_censo", length = 64)
	private String hashRaizCenso;

	@Column(name = "fecha_congelamiento")
	private LocalDateTime fechaCongelamiento;

	@Column(name = "actor_congelamiento", length = 120)
	private String actorCongelamiento;

	public EstadoCongelamientoCenso() {
	}

	public EstadoCongelamientoCenso(Long eleccionId, String estadoEleccion, boolean censoCongelado,
			String hashRaizCenso, LocalDateTime fechaCongelamiento, String actorCongelamiento) {
		this.eleccionId = eleccionId;
		this.estadoEleccion = estadoEleccion;
		this.censoCongelado = censoCongelado;
		this.hashRaizCenso = hashRaizCenso;
		this.fechaCongelamiento = fechaCongelamiento;
		this.actorCongelamiento = actorCongelamiento;
	}

	public Long getEleccionId() {
		return eleccionId;
	}

	public String getEstadoEleccion() {
		return estadoEleccion;
	}

	public void setEstadoEleccion(String estadoEleccion) {
		this.estadoEleccion = estadoEleccion;
	}

	public boolean isCensoCongelado() {
		return censoCongelado;
	}

	public void setCensoCongelado(boolean censoCongelado) {
		this.censoCongelado = censoCongelado;
	}

	public String getHashRaizCenso() {
		return hashRaizCenso;
	}

	public void setHashRaizCenso(String hashRaizCenso) {
		this.hashRaizCenso = hashRaizCenso;
	}

	public LocalDateTime getFechaCongelamiento() {
		return fechaCongelamiento;
	}

	public void setFechaCongelamiento(LocalDateTime fechaCongelamiento) {
		this.fechaCongelamiento = fechaCongelamiento;
	}

	public String getActorCongelamiento() {
		return actorCongelamiento;
	}

	public void setActorCongelamiento(String actorCongelamiento) {
		this.actorCongelamiento = actorCongelamiento;
	}
}