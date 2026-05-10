package com.selloLegitimo.GestionPreElectoral.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import com.selloLegitimo.GestionPreElectoral.modelo.EstadoCongelamientoCenso;

public interface EstadoCongelamientoCensoRepositorio extends JpaRepository<EstadoCongelamientoCenso, Long> {
}