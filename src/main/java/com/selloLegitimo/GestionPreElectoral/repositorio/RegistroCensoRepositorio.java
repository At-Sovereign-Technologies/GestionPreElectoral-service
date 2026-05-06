package com.selloLegitimo.GestionPreElectoral.repositorio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.selloLegitimo.GestionPreElectoral.modelo.Ciudadano;
import com.selloLegitimo.GestionPreElectoral.modelo.EstadoCenso;
import com.selloLegitimo.GestionPreElectoral.modelo.RegistroCenso;

public interface RegistroCensoRepositorio extends JpaRepository<RegistroCenso, Long> {

	List<RegistroCenso> findByEleccionIdOrderByFechaActualizacionDesc(Long eleccionId);

	Optional<RegistroCenso> findByEleccionIdAndCiudadano(Long eleccionId, Ciudadano ciudadano);

	@Query("""
		SELECT r FROM RegistroCenso r JOIN r.ciudadano c
		WHERE r.eleccionId = :eleccionId
		AND (:estado IS NULL OR r.estado = :estado)
		AND (:search IS NULL OR :search = ''
			OR LOWER(c.numeroDocumento) LIKE LOWER(CONCAT('%', :search, '%'))
			OR LOWER(c.nombres) LIKE LOWER(CONCAT('%', :search, '%'))
			OR LOWER(c.apellidos) LIKE LOWER(CONCAT('%', :search, '%')))
		""")
	Page<RegistroCenso> buscarPaginado(@Param("eleccionId") Long eleccionId,
			@Param("estado") EstadoCenso estado,
			@Param("search") String search,
			Pageable pageable);

	@Query("SELECT r.estado, COUNT(r) FROM RegistroCenso r WHERE r.eleccionId = :eleccionId GROUP BY r.estado")
	List<Object[]> contarPorEstado(@Param("eleccionId") Long eleccionId);
	@Query("""
		SELECT COUNT(r) FROM RegistroCenso r JOIN r.ciudadano c
		WHERE r.eleccionId = :eleccionId
		AND c.numeroDocumento = :documento
		AND r.estado = 'HABILITADO'
		""")
	long countByEleccionIdAndDocumentoHabilitado(@Param("eleccionId") Long eleccionId,
			@Param("documento") String documento);
}
