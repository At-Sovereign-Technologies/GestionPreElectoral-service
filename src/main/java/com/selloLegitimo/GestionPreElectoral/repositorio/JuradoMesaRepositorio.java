package com.selloLegitimo.GestionPreElectoral.repositorio;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.selloLegitimo.GestionPreElectoral.modelo.JuradoMesa;

public interface JuradoMesaRepositorio extends JpaRepository<JuradoMesa, Long> {
	@Query("SELECT j FROM JuradoMesa j WHERE j.mesaId = :mesaId AND j.tokenAcceso IN :tokens")
	List<JuradoMesa> encontrarPorMesaIdYTokens(@Param("mesaId") Long mesaId, @Param("tokens") Collection<String> tokens);

	List<JuradoMesa> findByMesaId(Long mesaId);
}