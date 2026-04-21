package com.selloLegitimo.GestionPreElectoral.repositorio;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.selloLegitimo.GestionPreElectoral.modelo.Ciudadano;

public interface CiudadanoRepositorio extends JpaRepository<Ciudadano, Long> {

	Optional<Ciudadano> findByTipoDocumentoAndNumeroDocumento(String tipoDocumento, String numeroDocumento);
}