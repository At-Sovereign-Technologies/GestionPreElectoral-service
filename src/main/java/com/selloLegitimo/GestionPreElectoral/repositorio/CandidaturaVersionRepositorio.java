package com.selloLegitimo.GestionPreElectoral.repositorio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.selloLegitimo.GestionPreElectoral.modelo.CandidaturaVersion;

@Repository
public interface CandidaturaVersionRepositorio extends JpaRepository<CandidaturaVersion, Long> {
	List<CandidaturaVersion> findByCandidaturaIdOrderByVersionNumberDesc(Long candidaturaId);
}
