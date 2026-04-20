package com.selloLegitimo.GestionPreElectoral.repositorio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.selloLegitimo.GestionPreElectoral.modelo.Ciudadano;
import com.selloLegitimo.GestionPreElectoral.modelo.RegistroCenso;

public interface RegistroCensoRepositorio extends JpaRepository<RegistroCenso, Long> {

	List<RegistroCenso> findByEleccionIdOrderByFechaActualizacionDesc(Long eleccionId);

	Optional<RegistroCenso> findByEleccionIdAndCiudadano(Long eleccionId, Ciudadano ciudadano);
}