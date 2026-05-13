package com.selloLegitimo.GestionPreElectoral.dto;

import java.time.OffsetDateTime;

import org.springframework.format.annotation.DateTimeFormat;

public class AuditLogFilterDto {

	private String actorId;
	private String entityType;

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	private OffsetDateTime inicio;

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	private OffsetDateTime fin;

	public String getActorId() {
		return actorId;
	}

	public void setActorId(String actorId) {
		this.actorId = actorId;
	}

	public String getActorIdOrDefault() {
		return actorId;
	}

	public String getEntityType() {
		return entityType;
	}

	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}

	public String getEntityTypeOrDefault() {
		return entityType;
	}

	public OffsetDateTime getInicio() {
		return inicio;
	}

	public void setInicio(OffsetDateTime inicio) {
		this.inicio = inicio;
	}

	public OffsetDateTime getFin() {
		return fin;
	}

	public void setFin(OffsetDateTime fin) {
		this.fin = fin;
	}
}
