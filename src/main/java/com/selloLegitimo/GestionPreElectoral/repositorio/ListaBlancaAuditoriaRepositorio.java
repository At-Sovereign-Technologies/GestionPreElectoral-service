package com.selloLegitimo.GestionPreElectoral.repositorio;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.selloLegitimo.GestionPreElectoral.modelo.ListaBlancaAuditoria;

public interface ListaBlancaAuditoriaRepositorio extends JpaRepository<ListaBlancaAuditoria, UUID> {

}
