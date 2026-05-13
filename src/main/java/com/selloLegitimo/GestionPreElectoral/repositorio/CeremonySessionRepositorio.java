package com.selloLegitimo.GestionPreElectoral.repositorio;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.selloLegitimo.GestionPreElectoral.modelo.CeremonySession;
import com.selloLegitimo.GestionPreElectoral.modelo.EstadoCeremonia;

@Repository
public interface CeremonySessionRepositorio extends JpaRepository<CeremonySession, UUID> {

    List<CeremonySession> findByStatus(EstadoCeremonia status);

    @Query("SELECT c FROM CeremonySession c WHERE c.status = :status AND c.expiresAt > CURRENT_TIMESTAMP")
    List<CeremonySession> findActiveByStatus(EstadoCeremonia status);

    @Query("SELECT c FROM CeremonySession c WHERE c.status = 'ACTIVE' AND c.expiresAt > CURRENT_TIMESTAMP")
    List<CeremonySession> findActiveCeremonies();
}