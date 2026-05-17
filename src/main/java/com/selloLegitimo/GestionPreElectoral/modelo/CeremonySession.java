package com.selloLegitimo.GestionPreElectoral.modelo;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "ceremony_session", schema = "gestion_pre_electoral")
public class CeremonySession {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "ceremony_type", nullable = false, length = 20)
    private TipoCeremonia ceremonyType = TipoCeremonia.APERTURA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiated_by", nullable = false)
    private ListaBlanca initiatedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EstadoCeremonia status = EstadoCeremonia.PENDING;

    @Column(name = "required_shards", nullable = false)
    private Integer requiredShards = 3;

    @Column(name = "submitted_shards", nullable = false)
    private Integer submittedShards = 0;

    @Column(name = "activated_at")
    private LocalDateTime activatedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public CeremonySession() {
        this.id = UUID.randomUUID();
        this.createdAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusMinutes(30);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public TipoCeremonia getCeremonyType() {
        return ceremonyType;
    }

    public void setCeremonyType(TipoCeremonia ceremonyType) {
        this.ceremonyType = ceremonyType;
    }

    public ListaBlanca getInitiatedBy() {
        return initiatedBy;
    }

    public void setInitiatedBy(ListaBlanca initiatedBy) {
        this.initiatedBy = initiatedBy;
    }

    public EstadoCeremonia getStatus() {
        return status;
    }

    public void setStatus(EstadoCeremonia status) {
        this.status = status;
    }

    public Integer getRequiredShards() {
        return requiredShards;
    }

    public void setRequiredShards(Integer requiredShards) {
        this.requiredShards = requiredShards;
    }

    public Integer getSubmittedShards() {
        return submittedShards;
    }

    public void setSubmittedShards(Integer submittedShards) {
        this.submittedShards = submittedShards;
    }

    public LocalDateTime getActivatedAt() {
        return activatedAt;
    }

    public void setActivatedAt(LocalDateTime activatedAt) {
        this.activatedAt = activatedAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    public boolean isReadyToActivate() {
        return this.submittedShards >= this.requiredShards;
    }
}