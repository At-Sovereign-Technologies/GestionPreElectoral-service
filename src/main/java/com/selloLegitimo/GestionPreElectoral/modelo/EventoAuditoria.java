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
import jakarta.persistence.Table;

@Entity
@Table(name = "auditoria_eventos", schema = "gestion_pre_electoral")
public class EventoAuditoria {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "aggregado_tipo", nullable = false, length = 50)
	private String aggregadoTipo;

	@Column(name = "aggregado_id", nullable = false)
	private Long aggregadoId;

	@Enumerated(EnumType.STRING)
	@Column(name = "tipo_evento", nullable = false, length = 50)
	private TipoEventoAuditoria tipoEvento;

	@Column(nullable = false, length = 120)
	private String actor;

	@Column(name = "payload_json", columnDefinition = "TEXT")
	private String payloadJson;

	@Column(name = "hash_integridad", nullable = false, length = 64)
	private String hashIntegridad;

	@Column(name = "fecha_evento", nullable = false)
	private LocalDateTime fechaEvento;

	public EventoAuditoria() {
	}

	public EventoAuditoria(String aggregadoTipo, Long aggregadoId, TipoEventoAuditoria tipoEvento,
			String actor, String payloadJson, String hashIntegridad) {
		this.aggregadoTipo = aggregadoTipo;
		this.aggregadoId = aggregadoId;
		this.tipoEvento = tipoEvento;
		this.actor = actor;
		this.payloadJson = payloadJson;
		this.hashIntegridad = hashIntegridad;
	}

	@PrePersist
	public void prePersist() {
		this.fechaEvento = LocalDateTime.now();
	}

	public Long getId() {
		return id;
	}

	public String getAggregadoTipo() {
		return aggregadoTipo;
	}

	public Long getAggregadoId() {
		return aggregadoId;
	}

	public TipoEventoAuditoria getTipoEvento() {
		return tipoEvento;
	}

	public String getActor() {
		return actor;
	}

	public String getPayloadJson() {
		return payloadJson;
	}

	public String getHashIntegridad() {
		return hashIntegridad;
	}

	public void setHashIntegridad(String hashIntegridad) {
		this.hashIntegridad = hashIntegridad;
	}

	public LocalDateTime getFechaEvento() {
		return fechaEvento;
	}
}
