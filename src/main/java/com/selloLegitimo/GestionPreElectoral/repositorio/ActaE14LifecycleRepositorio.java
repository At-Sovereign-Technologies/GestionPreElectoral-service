package com.selloLegitimo.GestionPreElectoral.repositorio;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.selloLegitimo.GestionPreElectoral.modelo.ActaE14Lifecycle;

@Repository
public interface ActaE14LifecycleRepositorio extends JpaRepository<ActaE14Lifecycle, UUID> {

	@Query("SELECT l FROM ActaE14Lifecycle l WHERE l.acta.uuid = :actaUuid ORDER BY l.versionNumber ASC")
	List<ActaE14Lifecycle> findByActaUuidOrderByVersionNumberAsc(@Param("actaUuid") UUID actaUuid);

	@Query("SELECT l FROM ActaE14Lifecycle l WHERE l.acta.uuid = :actaUuid ORDER BY l.versionNumber DESC")
	List<ActaE14Lifecycle> findByActaUuidOrderByVersionNumberDesc(@Param("actaUuid") UUID actaUuid);

	@Query("SELECT l FROM ActaE14Lifecycle l WHERE l.acta.uuid = :actaUuid ORDER BY l.versionNumber DESC LIMIT 1")
	Optional<ActaE14Lifecycle> findLatestByActaUuid(@Param("actaUuid") UUID actaUuid);

	@Query("SELECT COALESCE(MAX(l.versionNumber), 0) FROM ActaE14Lifecycle l WHERE l.acta.uuid = :actaUuid")
	Integer findMaxVersionNumberByActaUuid(@Param("actaUuid") UUID actaUuid);

	@Query("SELECT l FROM ActaE14Lifecycle l WHERE l.uuid = :uuid")
	Optional<ActaE14Lifecycle> findByUuid(@Param("uuid") UUID uuid);
}
