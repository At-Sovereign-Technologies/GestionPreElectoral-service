package com.selloLegitimo.GestionPreElectoral.repositorio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.selloLegitimo.GestionPreElectoral.modelo.Candidatura;
import com.selloLegitimo.GestionPreElectoral.modelo.EstadoCandidatura;

public interface CandidaturaRepositorio extends JpaRepository<Candidatura, Long> {

	boolean existsByEleccionIdAndDocumento(Long eleccionId, String documento);

	boolean existsByEleccionIdAndDocumentoAndIdNot(Long eleccionId, String documento, Long id);

	List<Candidatura> findByEleccionIdOrderByFechaInscripcionDesc(Long eleccionId);

	List<Candidatura> findByEleccionIdAndEstadoIn(Long eleccionId, List<EstadoCandidatura> estados);

	@Query("SELECT c.documento FROM Candidatura c WHERE c.eleccionId = :eleccionId")
	List<String> findDocumentosByEleccionId(@Param("eleccionId") Long eleccionId);
}