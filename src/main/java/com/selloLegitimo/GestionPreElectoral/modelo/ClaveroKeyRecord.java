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
@Table(name = "clavero_key_record", schema = "gestion_pre_electoral")
public class ClaveroKeyRecord {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "magistrado_id", nullable = false)
    private ListaBlanca magistrado;

    @Column(name = "shard_index", nullable = false)
    private Integer shardIndex;

    @Column(name = "shard_fingerprint", nullable = false, length = 64)
    private String shardFingerprint;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_method", nullable = false, length = 30)
    private MetodoEntregaShard deliveryMethod = MetodoEntregaShard.PEM_FILE;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "last_used_ceremony_id")
    private UUID lastUsedCeremonyId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public ClaveroKeyRecord() {
        this.createdAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public ListaBlanca getMagistrado() {
        return magistrado;
    }

    public void setMagistrado(ListaBlanca magistrado) {
        this.magistrado = magistrado;
    }

    public Integer getShardIndex() {
        return shardIndex;
    }

    public void setShardIndex(Integer shardIndex) {
        this.shardIndex = shardIndex;
    }

    public String getShardFingerprint() {
        return shardFingerprint;
    }

    public void setShardFingerprint(String shardFingerprint) {
        this.shardFingerprint = shardFingerprint;
    }

    public MetodoEntregaShard getDeliveryMethod() {
        return deliveryMethod;
    }

    public void setDeliveryMethod(MetodoEntregaShard deliveryMethod) {
        this.deliveryMethod = deliveryMethod;
    }

    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(LocalDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public UUID getLastUsedCeremonyId() {
        return lastUsedCeremonyId;
    }

    public void setLastUsedCeremonyId(UUID lastUsedCeremonyId) {
        this.lastUsedCeremonyId = lastUsedCeremonyId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}