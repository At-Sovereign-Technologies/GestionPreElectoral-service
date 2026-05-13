package com.selloLegitimo.GestionPreElectoral.repositorio;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.selloLegitimo.GestionPreElectoral.modelo.ClaveroKeyRecord;

@Repository
public interface ClaveroKeyRecordRepositorio extends JpaRepository<ClaveroKeyRecord, UUID> {

    Optional<ClaveroKeyRecord> findByMagistradoIdAndShardIndex(UUID magistradoId, Integer shardIndex);

    List<ClaveroKeyRecord> findByMagistradoId(UUID magistradoId);
}