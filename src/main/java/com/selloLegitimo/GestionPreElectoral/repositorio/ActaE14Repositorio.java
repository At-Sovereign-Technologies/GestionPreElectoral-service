package com.selloLegitimo.GestionPreElectoral.repositorio;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.selloLegitimo.GestionPreElectoral.modelo.ActaE14;

@Repository
public interface ActaE14Repositorio extends JpaRepository<ActaE14, UUID> {

	@Query("SELECT a FROM ActaE14 a WHERE a.uuid = :uuid")
	Optional<ActaE14> findByUuid(@Param("uuid") UUID uuid);

	@Query("SELECT a FROM ActaE14 a WHERE a.mesaId = :mesaId ORDER BY a.createdAt DESC")
	List<ActaE14> findByMesaId(@Param("mesaId") String mesaId);

	@Query("SELECT a FROM ActaE14 a WHERE a.eleccionId = :eleccionId ORDER BY a.createdAt DESC")
	List<ActaE14> findByEleccionId(@Param("eleccionId") Long eleccionId);
}
