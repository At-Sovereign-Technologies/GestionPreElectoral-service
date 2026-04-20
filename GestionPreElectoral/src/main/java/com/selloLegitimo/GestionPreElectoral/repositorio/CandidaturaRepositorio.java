package com.selloLegitimo.GestionPreElectoral.repositorio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.selloLegitimo.GestionPreElectoral.modelo.Candidatura;

public interface CandidaturaRepositorio extends JpaRepository<Candidatura, Long> {

	boolean existsByEleccionIdAndDocumento(Long eleccionId, String documento);

	boolean existsByEleccionIdAndDocumentoAndIdNot(Long eleccionId, String documento, Long id);

	List<Candidatura> findByEleccionIdOrderByFechaInscripcionDesc(Long eleccionId);
}