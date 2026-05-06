package com.selloLegitimo.GestionPreElectoral.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.selloLegitimo.GestionPreElectoral.modelo.EventoAuditoria;

import java.util.Optional;

@Repository
public interface EventoAuditoriaRepositorio extends JpaRepository<EventoAuditoria, Long> {
	Optional<EventoAuditoria> findFirstByOrderByIdDesc();

	Optional<EventoAuditoria> findFirstByAggregadoTipoAndAggregadoIdAndTipoEventoOrderByIdDesc(
			String aggregadoTipo, Long aggregadoId, String tipoEvento);
}
