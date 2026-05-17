package com.selloLegitimo.GestionPreElectoral.repositorio;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.selloLegitimo.GestionPreElectoral.modelo.RegistroAuditoria;

@Repository
public interface RegistroAuditoriaRepositorio extends JpaRepository<RegistroAuditoria, Long> {

	Optional<RegistroAuditoria> findFirstByOrderByIdDesc();

	@Query("SELECT r FROM RegistroAuditoria r WHERE r.actorId = :actorId ORDER BY r.timestampNtp DESC")
	Page<RegistroAuditoria> findByActorId(@Param("actorId") String actorId, Pageable pageable);

	@Query("SELECT r FROM RegistroAuditoria r WHERE r.entityType = :entityType ORDER BY r.timestampNtp DESC")
	Page<RegistroAuditoria> findByEntityType(@Param("entityType") String entityType, Pageable pageable);

	@Query("SELECT r FROM RegistroAuditoria r WHERE r.actorId = :actorId AND r.entityType = :entityType ORDER BY r.timestampNtp DESC")
	Page<RegistroAuditoria> findByActorIdAndEntityType(@Param("actorId") String actorId, @Param("entityType") String entityType, Pageable pageable);

	@Query("SELECT r FROM RegistroAuditoria r WHERE r.timestampNtp >= :inicio AND r.timestampNtp <= :fin ORDER BY r.timestampNtp DESC")
	Page<RegistroAuditoria> findByDateRange(@Param("inicio") OffsetDateTime inicio, @Param("fin") OffsetDateTime fin, Pageable pageable);

	@Query("SELECT r FROM RegistroAuditoria r WHERE r.actorId = :actorId AND r.entityType = :entityType AND r.timestampNtp >= :inicio AND r.timestampNtp <= :fin ORDER BY r.timestampNtp DESC")
	Page<RegistroAuditoria> findByFilters(
			@Param("actorId") String actorId,
			@Param("entityType") String entityType,
			@Param("inicio") OffsetDateTime inicio,
			@Param("fin") OffsetDateTime fin,
			Pageable pageable);

	@Query("SELECT r FROM RegistroAuditoria r WHERE r.entityType = :entityType AND r.entityId = :entityId ORDER BY r.timestampNtp ASC")
	java.util.List<RegistroAuditoria> findByEntityTypeAndEntityIdOrderByTimestampAsc(
			@Param("entityType") String entityType, @Param("entityId") String entityId);
}
